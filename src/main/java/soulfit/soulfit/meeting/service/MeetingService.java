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
import soulfit.soulfit.meeting.dto.*;
import soulfit.soulfit.meeting.dto.ai.AiRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiResponseDto;
import soulfit.soulfit.meeting.dto.ai.AiReviewResponseDto;
import soulfit.soulfit.meeting.repository.*;
import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.UserProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final AiReviewAnalysisService aiReviewAnalysisService;


    public List<MeetingResponseDto> getRecommendedMeetings(UserAuth user) {
        // This method returns a simplified DTO, so we keep the original `from` method for now
        // Or create a new simplified DTO for lists. For this task, we assume it's acceptable.
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
                        // Using a temporary builder as the static `from` is removed.
                        MeetingResponseDto dto = MeetingResponseDto.builder()
                                .id(meeting.getId())
                                .title(meeting.getTitle())
                                .category(meeting.getCategory())
                                .status(meeting.getMeetingStatus())
                                .imageUrls(meeting.getImages().stream().map(MeetingImage::getImageUrl).collect(Collectors.toList()))
                                .build();
                        dto.setRecommendationReasons(reasonsMap.get(meeting.getId()));
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Page<MeetingResponseDto> getAllMeetings(Pageable pageable){
        return meetingRepository.findAll(pageable).map(this::mapToSimpleDto);
    }

    public MeetingResponseDto getMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모임 없음"));

        // Host Info
        UserAuth host = meeting.getHost();
        String hostProfileImageUrl = Optional.ofNullable(host.getUserProfile())
                .map(UserProfile::getProfileImageUrl)
                .orElse(null);

        // D-day Badge
        String ddayBadge = "모집 마감";
        if (meeting.getRecruitDeadline().isAfter(LocalDateTime.now())) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), meeting.getRecruitDeadline().toLocalDate());
            ddayBadge = (daysLeft == 0) ? "D-day" : "D-" + daysLeft;
        }

        // Full Address
        Location location = meeting.getLocation();
        String fullAddress = String.format("%s %s %s %s",
                location.getCity(), location.getDistrict(), location.getAddress(), location.getDetailAddress()).trim();

        // Review Info
        List<MeetingReview> reviews = meeting.getReviews();
        int reviewCount = reviews.size();
        double reviewAvg = reviews.stream()
                .mapToDouble(MeetingReview::getMeetingRating)
                .average()
                .orElse(0.0);
        reviewAvg = Math.round(reviewAvg * 10.0) / 10.0; // 소수점 첫째 자리까지

        List<MeetingReviewResponseDto> reviewDtos = reviews.stream()
                .map(MeetingReviewResponseDto::from)
                .collect(Collectors.toList());

        // Review Summary (AI)
        // TODO: AI 리뷰 요약 기능 연동 필요. 현재는 타임아웃 문제로 비활성화.
        String reviewSummary = "리뷰 요약을 가져오는 중입니다.";
//        if (reviewCount > 0) {
//            try {
//                AiReviewResponseDto summaryResponse = aiReviewAnalysisService.analyzeReviewsByRestTemplate(reviews);
//                reviewSummary = summaryResponse.getSummary();
//            } catch (Exception e) {
//                // AI 서버 실패 시 로그를 남기거나 기본값을 사용
//                reviewSummary = "리뷰 요약을 가져오는 데 실패했습니다.";
//            }
//        }

        // Participant Stats
        ParticipantStatsDto participantStats = calculateParticipantStats(meeting);

        return MeetingResponseDto.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .category(meeting.getCategory())
                .hostName(host.getUsername())
                .hostProfileImageUrl(hostProfileImageUrl)
                .imageUrls(meeting.getImages().stream().map(MeetingImage::getImageUrl).collect(Collectors.toList()))
                .keywords(meeting.getKeywords().stream().map(Keyword::getName).collect(Collectors.toList()))
                .ddayBadge(ddayBadge)
                .schedules(meeting.getSchedules())
                .fullAddress(fullAddress)
                .canPickup(meeting.isCanPickup())
                .meetingTime(meeting.getMeetingTime())
                .duration(meeting.getDuration())
                .recruitDeadline(meeting.getRecruitDeadline())
                .maxParticipants(meeting.getMaxParticipants())
                .currentParticipants(meeting.getCurrentParticipants())
                .participantStats(participantStats)
                .reviewCount(reviewCount)
                .reviewAvg(reviewAvg)
                .reviewSummary(reviewSummary)
                .reviews(reviewDtos)
                .supplies(meeting.getSupplies())
                .pricePerPerson(meeting.getFee())
                .feeDescription(meeting.getFeeDescription())
                .status(meeting.getMeetingStatus())
                .createdAt(meeting.getCreatedAt())
                .build();
    }

    private ParticipantStatsDto calculateParticipantStats(Meeting meeting) {
        List<UserProfile> approvedUserProfiles = meeting.getMeetingParticipants().stream()
                .filter(p -> p.getApprovalStatus() == ApprovalStatus.APPROVED)
                .map(MeetingParticipant::getUser)
                .filter(Objects::nonNull)
                .map(UserAuth::getUserProfile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (approvedUserProfiles.isEmpty()) {
            return ParticipantStatsDto.builder()
                    .malePercent(0)
                    .femalePercent(0)
                    .ageGroupDistribution(Collections.emptyMap())
                    .build();
        }

        long totalParticipants = approvedUserProfiles.size();
        long maleCount = approvedUserProfiles.stream().filter(p -> p.getGender() == Gender.MALE).count();

        double malePercent = (double) maleCount / totalParticipants * 100;
        double femalePercent = 100 - malePercent;

        Map<String, Long> ageGroupCounts = approvedUserProfiles.stream()
                .map(p -> getAgeBand(p.getBirthDate()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Double> ageGroupDistribution = ageGroupCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (double) entry.getValue() / totalParticipants * 100
                ));

        return ParticipantStatsDto.builder()
                .malePercent(Math.round(malePercent))
                .femalePercent(Math.round(femalePercent))
                .ageGroupDistribution(ageGroupDistribution)
                .build();
    }

    private String getAgeBand(LocalDate birthDate) {
        if (birthDate == null) return "기타";
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 20) return "10대";
        if (age < 30) return "20대";
        if (age < 40) return "30대";
        if (age < 50) return "40대";
        return "50대 이상";
    }

    @Transactional
    public Meeting createMeeting(MeetingRequestDto requestDto, UserAuth userAuth) {
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
        // This also needs to be adjusted to not use `from`
        List<Meeting> meetings = meetingRepository.findByTitleContainingIgnoreCase(keyword);
        return meetings.stream()
                .map(m -> MeetingResponseDto.builder().id(m.getId()).title(m.getTitle()).build())
                .collect(Collectors.toList());
    }

        public Page<MeetingResponseDto> filterMeetings(SearchFilter filter, Pageable pageable) {

            return meetingRepository.search(filter, pageable).map(this::mapToSimpleDto);

        }

    

        private Meeting getMeetingOrThrow(Long id) {

            return meetingRepository.findById(id)

                    .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없음"));

        }

    

    

        @Transactional(readOnly = true)

        public Page<MeetingResponseDto> getParticipatedMeetings(UserAuth user, Pageable pageable) {

            Pageable sortedPageable  = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "meeting.meetingTime"));

            return meetingParticipantRepository.findMeetingUserParticipated(user, sortedPageable).map(this::mapToSimpleDto);

        }

    

        private MeetingResponseDto mapToSimpleDto(Meeting meeting) {

            return MeetingResponseDto.builder()

                    .id(meeting.getId())

                    .title(meeting.getTitle())

                    .category(meeting.getCategory())

                    .status(meeting.getMeetingStatus())

                    .imageUrls(meeting.getImages().stream().map(MeetingImage::getImageUrl).collect(Collectors.toList()))

                    .build();

        }
    
    // Methods like getPopularMeetings and getRecentMeetings also use `from` and need adjustment
    // For the scope of this task, I'm focusing on getMeetingById, but a full implementation
    // would require creating a simplified DTO for lists or adjusting these methods.
    
    public List<MeetingResponseDto> getPopularMeetings(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "bookmarkCount"));
        return getAllMeetings(sortedPageable).stream()
                .map(m -> MeetingResponseDto.builder().id(m.getId()).title(m.getTitle()).build())
                .collect(Collectors.toList());
    }

    public List<MeetingResponseDto> getRecentMeetings(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return getAllMeetings(sortedPageable).stream()
                .map(m -> MeetingResponseDto.builder().id(m.getId()).title(m.getTitle()).build())
                .collect(Collectors.toList());
    }
}
