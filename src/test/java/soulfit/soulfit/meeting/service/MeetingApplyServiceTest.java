package soulfit.soulfit.meeting.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingQuestion;
import soulfit.soulfit.meeting.domain.QuestionType;
import soulfit.soulfit.meeting.dto.MeetingApplicantDto;
import soulfit.soulfit.meeting.dto.MeetingQuestionDto;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.ApprovalStatus;
import soulfit.soulfit.meeting.domain.MeetingParticipant;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingQuestionRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingApplyServiceTest {

    @InjectMocks
    private MeetingApplyService meetingApplyService;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingQuestionRepository meetingQuestionRepository;

    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("모임 질문 추가 테스트")
    void addMeetingQuestions() {
        // given
        long meetingId = 1L;
        Meeting meeting = Meeting.builder().id(meetingId).build();
        MeetingQuestionDto.QuestionItem item1 = new MeetingQuestionDto.QuestionItem("주관식 질문", QuestionType.TEXT, 1, null);
        MeetingQuestionDto.QuestionItem item2 = new MeetingQuestionDto.QuestionItem("객관식 질문", QuestionType.MULTIPLE_CHOICE, 2, List.of("선택1", "선택2"));
        MeetingQuestionDto.Request request = new MeetingQuestionDto.Request(List.of(item1, item2));

        when(meetingRepository.findById(anyLong())).thenReturn(Optional.of(meeting));

        // when
        meetingApplyService.addMeetingQuestions(meetingId, request);

        // then
        verify(meetingRepository).findById(meetingId);
        verify(meetingQuestionRepository).saveAll(any(List.class));
    }

    @Test
    @DisplayName("모임 질문 조회 테스트")
    void getMeetingQuestions() {
        // given
        long meetingId = 1L;
        MeetingQuestion question1 = MeetingQuestion.createMeetingQuestion("주관식 질문", QuestionType.TEXT, 1, null);
        MeetingQuestion question2 = MeetingQuestion.createMeetingQuestion("객관식 질문", QuestionType.MULTIPLE_CHOICE, 2, List.of("선택1", "선택2"));
        List<MeetingQuestion> questions = List.of(question1, question2);

        when(meetingQuestionRepository.findByMeetingId(anyLong())).thenReturn(questions);

        // when
        List<MeetingQuestionDto.Response> responses = meetingApplyService.getMeetingQuestions(meetingId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getQuestionText()).isEqualTo("주관식 질문");
        assertThat(responses.get(0).getChoices()).isEmpty();
        assertThat(responses.get(1).getQuestionText()).isEqualTo("객관식 질문");
        assertThat(responses.get(1).getChoices()).hasSize(2).contains("선택1", "선택2");
        verify(meetingQuestionRepository).findByMeetingId(meetingId);
    }

    @Test
    @DisplayName("참가 신청 승인 성공")
    void approveMeetingApplication_success() {
        // given
        long meetingId = 1L;
        long userId = 100L;

        Meeting meeting = Meeting.builder().id(meetingId).maxParticipants(5).currentParticipants(0).build();
        UserAuth user = new UserAuth("testuser", "password", "test@example.com");
        user.setId(userId);
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setApprovalStatus(ApprovalStatus.PENDING);

        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.of(participant));
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // when
        meetingApplyService.approveMeetingApplication(meetingId, userId);

        // then
        assertThat(participant.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(meeting.getCurrentParticipants()).isEqualTo(1);
        verify(meetingParticipantRepository).save(participant);
        verify(meetingRepository).save(meeting);
    }

    @Test
    @DisplayName("참가 신청 승인 실패 - 미팅을 찾을 수 없음")
    void approveMeetingApplication_meetingNotFound() {
        // given
        long meetingId = 1L;
        long userId = 100L;

        MeetingParticipant participant = new MeetingParticipant();
        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.of(participant));
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                meetingApplyService.approveMeetingApplication(meetingId, userId)
        );
    }

    @Test
    @DisplayName("참가 신청 승인 실패 - 참가자를 찾을 수 없음")
    void approveMeetingApplication_participantNotFound() {
        // given
        long meetingId = 1L;
        long userId = 100L;

        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.empty());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                meetingApplyService.approveMeetingApplication(meetingId, userId)
        );
    }

    @Test
    @DisplayName("참가 신청 승인 실패 - 이미 승인된 참가자")
    void approveMeetingApplication_alreadyApproved() {
        // given
        long meetingId = 1L;
        long userId = 100L;

        Meeting meeting = Meeting.builder().id(meetingId).maxParticipants(5).currentParticipants(0).build();
        UserAuth user = new UserAuth("testuser", "password", "test@example.com");
        user.setId(userId);
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setApprovalStatus(ApprovalStatus.APPROVED);

        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.of(participant));
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                meetingApplyService.approveMeetingApplication(meetingId, userId)
        );
    }

    @Test
    @DisplayName("참가 신청 승인 실패 - 최대 참가자 수 초과")
    void approveMeetingApplication_maxParticipantsExceeded() {
        // given
        long meetingId = 1L;
        long userId = 100L;

        Meeting meeting = Meeting.builder().id(meetingId).maxParticipants(1).currentParticipants(1).build(); // Already full
        UserAuth user = new UserAuth("testuser", "password", "test@example.com");
        user.setId(userId);
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setApprovalStatus(ApprovalStatus.PENDING);

        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.of(participant));
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                meetingApplyService.approveMeetingApplication(meetingId, userId)
        );
    }

    @Test
    @DisplayName("참가 신청자 목록 조회 성공 - 주최자")
    void getApplicantsForMeeting_success() {
        // given
        long meetingId = 1L;
        long hostId = 1L;
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(hostId);

        Meeting meeting = Meeting.builder().id(meetingId).host(hostUser).build();

        UserAuth applicant1 = new UserAuth("applicant1", "pass1", "app1@example.com");
        applicant1.setId(2L);
        MeetingParticipant participant1 = new MeetingParticipant();
        participant1.setMeeting(meeting);
        participant1.setUser(applicant1);
        participant1.setApprovalStatus(ApprovalStatus.PENDING);

        UserAuth applicant2 = new UserAuth("applicant2", "pass2", "app2@example.com");
        applicant2.setId(3L);
        MeetingParticipant participant2 = new MeetingParticipant();
        participant2.setMeeting(meeting);
        participant2.setUser(applicant2);
        participant2.setApprovalStatus(ApprovalStatus.APPROVED);

        List<MeetingParticipant> participants = List.of(participant1, participant2);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingParticipantRepository.findByMeetingId(meetingId)).thenReturn(participants);

        // when
        List<MeetingApplicantDto> result = meetingApplyService.getApplicantsForMeeting(meetingId, hostUser);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(applicant1.getId());
        assertThat(result.get(1).getUserId()).isEqualTo(applicant2.getId());
        verify(meetingRepository).findById(meetingId);
        verify(meetingParticipantRepository).findByMeetingId(meetingId);
    }

    @Test
    @DisplayName("참가 신청자 목록 조회 실패 - 주최자가 아님")
    void getApplicantsForMeeting_notHost() {
        // given
        long meetingId = 1L;
        long hostId = 1L;
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(hostId);

        UserAuth nonHostUser = new UserAuth("nonHost", "password", "nonhost@example.com");
        nonHostUser.setId(2L);

        Meeting meeting = Meeting.builder().id(meetingId).host(hostUser).build();

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(AccessDeniedException.class, () ->
                meetingApplyService.getApplicantsForMeeting(meetingId, nonHostUser)
        );
    }

    @Test
    @DisplayName("참가 신청자 목록 조회 실패 - 모임을 찾을 수 없음")
    void getApplicantsForMeeting_meetingNotFound() {
        // given
        long meetingId = 1L;
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(1L);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                meetingApplyService.getApplicantsForMeeting(meetingId, hostUser)
        );
    }

    @Test
    @DisplayName("참가 신청 거절 성공")
    void rejectMeetingApplication_success() {
        // given
        long meetingId = 1L;
        long userId = 100L;
        String rejectionReason = "모임 주제와 맞지 않습니다.";

        long hostId = 1L;
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(hostId);

        Meeting meeting = Meeting.builder().id(meetingId).host(hostUser).build();
        UserAuth applicant = new UserAuth("applicant", "pass", "app@example.com");
        applicant.setId(userId);
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(applicant);
        participant.setApprovalStatus(ApprovalStatus.PENDING);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.of(participant));

        // when
        meetingApplyService.rejectMeetingApplication(meetingId, userId, rejectionReason, hostUser);

        // then
        assertThat(participant.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(participant.getRejectedReason()).isEqualTo(rejectionReason);
        verify(meetingParticipantRepository).save(participant);
    }

    @Test
    @DisplayName("참가 신청 거절 실패 - 주최자가 아님")
    void rejectMeetingApplication_notHost() {
        // given
        long meetingId = 1L;
        long userId = 100L;
        String rejectionReason = "모임 주제와 맞지 않습니다.";

        long hostId = 1L;
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(hostId);

        UserAuth nonHostUser = new UserAuth("nonHost", "password", "nonhost@example.com");
        nonHostUser.setId(2L);

        Meeting meeting = Meeting.builder().id(meetingId).host(hostUser).build();

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(AccessDeniedException.class, () ->
                meetingApplyService.rejectMeetingApplication(meetingId, userId, rejectionReason, nonHostUser)
        );
    }

    @Test
    @DisplayName("참가 신청 거절 실패 - 모임을 찾을 수 없음")
    void rejectMeetingApplication_meetingNotFound() {
        // given
        long meetingId = 1L;
        long userId = 100L;
        String rejectionReason = "모임 주제와 맞지 않습니다.";
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(1L);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                meetingApplyService.rejectMeetingApplication(meetingId, userId, rejectionReason, hostUser)
        );
    }

    @Test
    @DisplayName("참가 신청 거절 실패 - 참가자를 찾을 수 없음")
    void rejectMeetingApplication_participantNotFound() {
        // given
        long meetingId = 1L;
        long userId = 100L;
        String rejectionReason = "모임 주제와 맞지 않습니다.";

        long hostId = 1L;
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(hostId);

        Meeting meeting = Meeting.builder().id(meetingId).host(hostUser).build();

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.empty());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                meetingApplyService.rejectMeetingApplication(meetingId, userId, rejectionReason, hostUser)
        );
    }

    @Test
    @DisplayName("참가 신청 거절 실패 - 이미 승인된 참가자")
    void rejectMeetingApplication_alreadyApproved() {
        // given
        long meetingId = 1L;
        long userId = 100L;
        String rejectionReason = "모임 주제와 맞지 않습니다.";

        long hostId = 1L;
        UserAuth hostUser = new UserAuth("host", "password", "host@example.com");
        hostUser.setId(hostId);

        Meeting meeting = Meeting.builder().id(meetingId).host(hostUser).build();
        UserAuth applicant = new UserAuth("applicant", "pass", "app@example.com");
        applicant.setId(userId);
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(applicant);
        participant.setApprovalStatus(ApprovalStatus.APPROVED);

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)).thenReturn(Optional.of(participant));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                meetingApplyService.rejectMeetingApplication(meetingId, userId, rejectionReason, hostUser)
        );
    }
}
