package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.client.AiMeetingClient;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.dto.MeetingRequestDto;
import soulfit.soulfit.meeting.dto.MeetingResponseDto;
import soulfit.soulfit.meeting.dto.MeetingUpdateRequestDto;
import soulfit.soulfit.meeting.dto.SearchFilter;
import soulfit.soulfit.meeting.dto.ai.AiRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiResponseDto;
import soulfit.soulfit.meeting.repository.MeetingBookmarkRepository;
import soulfit.soulfit.meeting.repository.MeetingKeywordRepository;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.meeting.domain.HostProFile;
import soulfit.soulfit.meeting.repository.HostProfileRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingImageService meetingImageService;
    private final MeetingKeywordRepository meetingKeyWordRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final HostProfileRepository hostProfileRepository;
    private final AiMeetingClient aiMeetingClient;
    private final MeetingBookmarkRepository meetingBookmarkRepository;


    public List<MeetingResponseDto> getRecommendedMeetings(UserAuth user) {
        Pageable limit = PageRequest.of(0, 5);
        List<Meeting> participatedMeetings = meetingParticipantRepository.findMeetingUserParticipated(user, limit).getContent();
        List<Meeting> bookmarkedMeetings = meetingBookmarkRepository.findByUserOrderByCreatedAtDesc(user, limit)
                .map(MeetingBookmark::getMeeting).getContent();

        AiRequestDto requestDto = AiRequestDto.builder()
                .userId(user.getId())
                .recentCategories(
                        participatedMeetings.stream()
                                .map(m -> m.getCategory().name())
                                .collect(Collectors.toList())
                )
                .recentKeywords(
                        participatedMeetings.stream()
                                .flatMap(m -> m.getKeywords().stream().map(Keyword::getName))
                                .collect(Collectors.toList())
                )
                .bookmarkedCategories(
                        bookmarkedMeetings.stream()
                                .map(m -> m.getCategory().name())
                                .collect(Collectors.toList())
                )
                .build();

        try {
            AiResponseDto response = aiMeetingClient.getRecommendations(requestDto);
            if (response == null || response.getRecommendations() == null || response.getRecommendations().isEmpty()) {
                return Collections.emptyList();
            }

            Map<Long, List<String>> reasonsMap = response.getRecommendations().stream()
                    .collect(Collectors.toMap(AiResponseDto.RecommendationItem::getMeetingId, AiResponseDto.RecommendationItem::getReasonKeywords));

            List<Long> recommendedIds = response.getRecommendations().stream()
                    .map(AiResponseDto.RecommendationItem::getMeetingId)
                    .collect(Collectors.toList());

            return meetingRepository.findAllById(recommendedIds).stream()
                    .map(meeting -> {
                        MeetingResponseDto dto = MeetingResponseDto.from(meeting);
                        dto.setRecommendationReasons(reasonsMap.get(meeting.getId()));
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // AI 서버 통신 실패 시 대체 로직 (예: 인기순 또는 최신순 반환)
            return getAllMeetings(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bookmarkCount")))
                    .stream()
                    .map(MeetingResponseDto::from)
                    .collect(Collectors.toList());
        }
    }

    // ... (기존 메서드들)

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

        hostProfileRepository.findById(managedUser.getId())
                .orElseGet(() -> {
                    HostProFile hostProFile = new HostProFile(managedUser, 0.0);
                    return hostProfileRepository.save(hostProFile);
                });

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

        if (requestDto.getMeetingTime() != null && requestDto.getMeetingTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("모임시간은 현재시간 이후여야 합니다.");
        }

        if (requestDto.getRecruitDeadline() != null && requestDto.getMeetingTime() != null &&
                requestDto.getRecruitDeadline().isAfter(requestDto.getMeetingTime())) {
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

    public Page<Meeting> filterMeetings(SearchFilter filter, Pageable pageable) {
        return meetingRepository.search(filter, pageable);
    }



    private Meeting getMeetingOrThrow(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없음"));
    }


    @Transactional(readOnly = true)
    public Page<Meeting> getParticipatedMeetings(UserAuth user, Pageable pageable) {
        Pageable sortedPageable  = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "meeting.meetingTime"));
        return meetingParticipantRepository.findMeetingUserParticipated(user, sortedPageable);
    }
}
