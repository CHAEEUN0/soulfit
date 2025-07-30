package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.dto.MeetingAnswerDto;
import soulfit.soulfit.meeting.dto.MeetingApplicantDto;
import soulfit.soulfit.meeting.dto.MeetingQuestionDto;
import soulfit.soulfit.meeting.dto.MeetingResponseDto;
import soulfit.soulfit.meeting.repository.*;
import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingApplyService {

    private final MeetingRepository meetingRepository;
    private final MeetingQuestionRepository meetingQuestionRepository;
    private final MeetingAnswerRepository meetingAnswerRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public void addMeetingQuestions(Long meetingId, MeetingQuestionDto.Request request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));

        List<MeetingQuestion> questions = request.getQuestions().stream()
                .map(MeetingQuestionDto.QuestionItem::toEntity)
                .peek(question -> question.setMeeting(meeting))
                .collect(Collectors.toList());

        meetingQuestionRepository.saveAll(questions);
    }

    @Transactional(readOnly = true)
    public List<MeetingQuestionDto.Response> getMeetingQuestions(Long meetingId) {
        List<MeetingQuestion> questions = meetingQuestionRepository.findByMeetingId(meetingId);
        return questions.stream()
                .map(MeetingQuestionDto.Response::new)
                .collect(Collectors.toList());
    }

    public void saveAnswers(Long meetingId, Long userId, MeetingAnswerDto.Request request) {
        MeetingParticipant participant = meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for meeting: " + meetingId + " and user: " + userId));

        List<MeetingAnswer> answers = request.getAnswers().stream().map(answerItem -> {
            MeetingQuestion question = meetingQuestionRepository.findById(answerItem.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + answerItem.getQuestionId()));

            return switch (question.getQuestionType()) {
                case TEXT -> MeetingAnswer.createTextAnswer(participant, question, answerItem.getTextAnswer());
                case MULTIPLE_CHOICE ->
                        MeetingAnswer.createChoiceAnswer(participant, question, answerItem.getSelectedChoices());
            };
        }).collect(Collectors.toList());

        meetingAnswerRepository.saveAll(answers);
    }

    @Transactional(readOnly = true)
    public List<MeetingAnswerDto.Response> getAllAnswers(Long meetingId) {
        List<MeetingAnswer> allAnswers = meetingAnswerRepository.findByParticipantMeetingId(meetingId);

        Map<MeetingParticipant, List<MeetingAnswer>> answersByParticipant = allAnswers.stream()
                .collect(Collectors.groupingBy(MeetingAnswer::getParticipant));

        return answersByParticipant.entrySet().stream().map(entry -> {
            MeetingParticipant participant = entry.getKey();
            List<MeetingAnswerDto.AnswerResponseItem> answerItems = entry.getValue().stream()
                    .map(MeetingAnswerDto.AnswerResponseItem::new)
                    .collect(Collectors.toList());
            return new MeetingAnswerDto.Response(
                    participant.getId(),
                    participant.getUser().getId(),
                    participant.getUser().getUsername(),
                    answerItems
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeetingAnswerDto.Response getUserAnswers(Long meetingId, Long userId) {
        List<MeetingAnswer> userAnswers = meetingAnswerRepository.findByParticipantMeetingIdAndParticipantUserId(meetingId, userId);
        if (userAnswers.isEmpty()) {
            return null; // or throw exception, depending on desired behavior
        }

        MeetingParticipant participant = userAnswers.get(0).getParticipant();
        List<MeetingAnswerDto.AnswerResponseItem> answerItems = userAnswers.stream()
                .map(MeetingAnswerDto.AnswerResponseItem::new)
                .collect(Collectors.toList());

        return new MeetingAnswerDto.Response(
                participant.getId(),
                participant.getUser().getId(),
                participant.getUser().getUsername(),
                answerItems
        );
    }

    public void joinMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));
        UserAuth user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check if user is already a participant
        if (meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId).isPresent()) {
            throw new IllegalArgumentException("User is already a participant in this meeting.");
        }

        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setApprovalStatus(ApprovalStatus.PENDING); // Or APPROVED, depending on business logic
        participant.setJoinedAt(LocalDateTime.now());

        meetingParticipantRepository.save(participant);

        Long hostId = meeting.getHost().getId();

        notificationService.sendNotification(
                hostId,
                NotificationType.JOIN_MEETING,
                "user joined!",
                "user" + user.getUsername() + " has joined your meeting[" + meeting.getTitle()+"]",
                meeting.getId()
        );

    }

    public void approveMeetingApplication(Long meetingId, Long userId) {
        MeetingParticipant participant = meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for meeting: " + meetingId + " and user: " + userId));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));

        // 참가자 승인 로직
        participant.approve();

        // 미팅 참가자 수 증가 로직
        meeting.addParticipant();

        meetingParticipantRepository.save(participant);
        meetingRepository.save(meeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingApplicantDto> getApplicantsForMeeting(Long meetingId, UserAuth userAuth) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));

        if (!meeting.getHost().getId().equals(userAuth.getId())) {
            throw new AccessDeniedException("모임 주최자만 참가 신청자 목록을 조회할 수 있습니다.");
        }

        List<MeetingParticipant> applicants = meetingParticipantRepository.findByMeetingId(meetingId);
        return applicants.stream()
                .map(MeetingApplicantDto::from)
                .collect(Collectors.toList());
    }

    public void rejectMeetingApplication(Long meetingId, Long userId, String rejectionReason, UserAuth userAuth) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));

        if (!meeting.getHost().getId().equals(userAuth.getId())) {
            throw new AccessDeniedException("모임 주최자만 참가 신청을 거절할 수 있습니다.");
        }

        MeetingParticipant participant = meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for meeting: " + meetingId + " and user: " + userId));

        if (participant.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 참가자는 거절할 수 없습니다.");
        }

        participant.setApprovalStatus(ApprovalStatus.REJECTED);
        participant.setRejectedReason(rejectionReason);
        meetingParticipantRepository.save(participant);
    }

    public List<MeetingResponseDto> getMeetingsByHost(UserAuth userAuth) {
        return meetingRepository.findByHost(userAuth).stream()
                .map(MeetingResponseDto::from)
                .collect(Collectors.toList());
    }

}