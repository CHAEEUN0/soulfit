package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.Keyword;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingImage;
import soulfit.soulfit.meeting.dto.MeetingRequestDto;
import soulfit.soulfit.meeting.dto.MeetingResponseDto;
import soulfit.soulfit.meeting.dto.MeetingUpdateRequestDto;
import soulfit.soulfit.meeting.repository.MeetingKeywordRepository;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingImageService meetingImageService;
    private final MeetingKeywordRepository meetingKeyWordRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    public Page<Meeting> getAllMeetings(Pageable pageable){
        return meetingRepository.findAll(pageable);
    }

    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모임 없음"));
    }

    @Transactional
    public Meeting createMeeting(MeetingRequestDto requestDto, UserAuth userAuth) {
        // 1. 준영속 userAuth의 id를 이용해 영속 상태의 user를 다시 조회한다.
        UserAuth managedUser = userRepository.findById(userAuth.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userAuth.getId()));

        if (requestDto.getMeetingTime().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("모임시간은 현재시간 이후여야 합니다.");
        }

        if (requestDto.getRecruitDeadline().isAfter(requestDto.getMeetingTime())){
            throw new IllegalArgumentException("마감시간은 모임시간보다 느릴 수 없습니다.");
        }

        // 2. 영속 상태의 managedUser를 사용해 연관관계를 설정한다.
        Meeting meeting = Meeting.createMeeting(requestDto, managedUser);

        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<MeetingImage> meetingImages = meetingImageService.uploadImages(requestDto.getImages(), meeting);
            meeting.getImages().addAll(meetingImages);
        }

        List<Keyword> keywords = meetingKeyWordRepository.findAllById(requestDto.getKeywordIds());
        meeting.setKeywords(new HashSet<>(keywords));


        return meetingRepository.save(meeting);

    }

    @Transactional
    public Meeting updateMeeting(Long meetingId, MeetingUpdateRequestDto requestDto, UserAuth user) {
        Meeting meeting = getMeetingOrThrow(meetingId);

        if (!meeting.getHost().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한 없음");
        }

        if (requestDto.getMeetingTime().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("모임시간은 현재시간 이후여야 합니다.");
        }

        if (requestDto.getRecruitDeadline().isAfter(requestDto.getMeetingTime())){
            throw new IllegalArgumentException("마감시간은 모임시간보다 느릴 수 없습니다.");
        }

        meeting.update(requestDto);

        meetingImageService.deleteImages(meeting.getImages());
        meeting.getImages().clear();

        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<MeetingImage> reviewImages = meetingImageService.uploadImages(requestDto.getImages(), meeting);
            meeting.getImages().addAll(reviewImages);
        }

        List<Long> keywordIds = requestDto.getKeywordIds();
        if (keywordIds != null && !keywordIds.isEmpty()) {
            List<Keyword> keywords = meetingKeyWordRepository.findAllById(keywordIds);
            meeting.setKeywords(new HashSet<>(keywords));
        }

        meeting.setUpdatedAt(LocalDateTime.now());

        return meeting;
    }

    @Transactional
    public void deleteMeeting(Long id, UserAuth user) {
        Meeting meeting = getMeetingOrThrow(id);

        if (!meeting.getHost().getId().equals(user.getId())) {
            throw new AccessDeniedException("삭제 권한 없음");
        }
        meetingImageService.deleteImages(meeting.getImages());
        meetingRepository.delete(meeting);
    }

    public List<MeetingResponseDto> searchMeetingsByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        List<Meeting> meetings = meetingRepository.findByTitleContainingIgnoreCase(keyword);
        return meetings.stream()
                .map(MeetingResponseDto::from)
                .toList();
    }



    private Meeting getMeetingOrThrow(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없음"));
    }


    @Transactional(readOnly = true)
    public Page<Meeting> getParticipatedMeetings(UserAuth user, Pageable pageable) {
        Pageable sortedPageable  = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "meetingTime"));
        return meetingParticipantRepository.findMeetingUserParticipated(user, sortedPageable);
    }


}
