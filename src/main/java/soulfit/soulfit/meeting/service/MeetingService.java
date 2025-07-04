package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.service.UserService;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingStatus;
import soulfit.soulfit.meeting.dto.MeetingFilter;
import soulfit.soulfit.meeting.dto.MeetingRequest;
import soulfit.soulfit.meeting.dto.MeetingResponse;
import soulfit.soulfit.meeting.repository.MeetingRepository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserService userService;

    @Transactional
    public List<MeetingResponse> getAllMeetings(){
        return meetingRepository.findAll().stream()
                .map(MeetingResponse::from)
                .collect(Collectors.toList());

    }

    public MeetingResponse getMeetingById(Long id){
        return meetingRepository.findById(id)
                .map(MeetingResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("모임 없음"));
    }

    @Transactional
    public Long createMeeting(UserAuth host,  MeetingRequest request) {

        Meeting meeting = Meeting.createMeeting(request, host);
        meetingRepository.save(meeting);

        return meeting.getId();
    }

    @Transactional
    public Long updateMeeting(Long meetingId, MeetingRequest request, UserAuth user) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없음"));


        if (!meeting.getHost().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한 없음");
        }
        // 수정
        meeting.update(request);
        return meeting.getId();
    }




    @Transactional
    public void deleteMeeting(Long id, UserAuth user) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없음"));

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
}
