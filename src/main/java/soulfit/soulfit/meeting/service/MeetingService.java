package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.dto.MeetingFilter;
import soulfit.soulfit.meeting.dto.MeetingRequest;
import soulfit.soulfit.meeting.dto.MeetingResponse;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public List<MeetingResponse> getAllMeetings(){
        return meetingRepository.findAll().stream()
                .map(MeetingResponse::from)
                .collect(Collectors.toList());

    }

    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모임 없음"));
    }

    @Transactional
    public Meeting createMeeting(UserAuth userAuth, MeetingRequest request) {
        // 1. 준영속 userAuth의 id를 이용해 영속 상태의 user를 다시 조회한다.
        UserAuth managedUser = userRepository.findById(userAuth.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userAuth.getId()));

        validMeetingRequest(request);

        // 2. 영속 상태의 managedUser를 사용해 연관관계를 설정한다.
        Meeting meeting = Meeting.createMeeting(request, managedUser);
        meetingRepository.save(meeting);

        return meeting;
    }

    @Transactional
    public Meeting updateMeeting(Long meetingId, MeetingRequest request, UserAuth user) {
        Meeting meeting = getMeetingOrThrow(meetingId);

        if (!meeting.getHost().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한 없음");
        }

        validMeetingRequest(request);

        meeting.update(request);
        return meeting;
    }

    @Transactional
    public void deleteMeeting(Long id, UserAuth user) {
        Meeting meeting = getMeetingOrThrow(id);

        if (!meeting.getHost().getId().equals(user.getId())) {
            throw new AccessDeniedException("삭제 권한 없음");
        }

        meetingRepository.delete(meeting);
    }

    public List<MeetingResponse> searchMeetingsByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        List<Meeting> meetings = meetingRepository.findByTitleContainingIgnoreCase(keyword);
        return meetings.stream()
                .map(MeetingResponse::from)
                .toList();
    }

    public List<MeetingResponse> filterMeetings(MeetingFilter filter) {

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            if (filter.getStartDate().isAfter(filter.getEndDate())) {
                throw new IllegalArgumentException("시작 날짜는 종료 날짜 이전이어야 합니다.");
            }
        }
        LocalDateTime start = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : LocalDate.MIN.atStartOfDay();

        LocalDateTime end = filter.getEndDate() != null ? filter.getEndDate().atTime(LocalTime.MAX) : LocalDate.MAX.atTime(LocalTime.MAX);

        List<Meeting> meetings = meetingRepository.filterMeetings(
                filter.getCategory(),
                filter.getCity(),
                start,
                end
        );

        return meetings.stream()
                .map(MeetingResponse::from)
                .toList();
    }

    private Meeting getMeetingOrThrow(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없음"));
    }

    private void validMeetingRequest(MeetingRequest request) {
        if (request.getMeetingTime().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("모임시간은 현재시간 이후여야 합니다.");
        }

        if (request.getRecruitDeadline().isAfter(request.getMeetingTime())){
            throw new IllegalArgumentException("마감시간은 모임시간보다 느릴 수 없습니다.");
        }
    }
}
