
package soulfit.soulfit.meeting.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.dashboard.dto.MeetingStatsResponseDto;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingReview;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingReviewRepository;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingReviewRepository meetingReviewRepository;
    private final UserRepository userRepository;

    public MeetingStatsResponseDto calculateMeetingStats(Long userId) {
        UserAuth user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Meeting> participatedMeetings = meetingParticipantRepository.findAllMeetingUserParticipated(user);
        List<MeetingReview> reviewsGiven = meetingReviewRepository.findAllByUser(user);
        Double avgRatingReceived = meetingReviewRepository.findAverageHostRatingByHostId(userId);


        // 기본 통계
        long totalMeetingsAttended = participatedMeetings.size();
        Map<String, Long> monthlyActivity = participatedMeetings.stream()
                .collect(Collectors.groupingBy(m -> m.getMeetingTime().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()));
        Map<String, Long> categoryDistribution = participatedMeetings.stream()
                .collect(Collectors.groupingBy(m -> m.getCategory().name(),
                        Collectors.counting()));
        String mostAttendedCategory = categoryDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 활동 시간/요일 패턴 분석
        String favoriteDayOfWeek = participatedMeetings.stream()
                .collect(Collectors.groupingBy(m -> m.getMeetingTime().getDayOfWeek(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().getDisplayName(TextStyle.FULL, Locale.KOREAN))
                .orElse(null);

        String favoriteTimeOfDay = participatedMeetings.stream()
                .collect(Collectors.groupingBy(m -> getTimeOfDay(m.getMeetingTime().getHour()), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 모임 선호도 분석
        double averageMeetingSize = participatedMeetings.stream()
                .mapToInt(Meeting::getMaxParticipants)
                .average()
                .orElse(0.0);

        String mostFrequentRegion = participatedMeetings.stream()
                .filter(m -> m.getLocation() != null && m.getLocation().getCity() != null)
                .collect(Collectors.groupingBy(m -> m.getLocation().getCity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 리뷰 기반 분석
        double averageRatingGiven = reviewsGiven.stream()
                .mapToDouble(MeetingReview::getMeetingRating)
                .average()
                .orElse(0.0);

        double averageRatingReceivedAsHost = OptionalDouble.of(avgRatingReceived != null ? avgRatingReceived : 0.0).orElse(0.0);


        return MeetingStatsResponseDto.builder()
                .totalMeetingsAttended(totalMeetingsAttended)
                .monthlyActivity(monthlyActivity)
                .categoryDistribution(categoryDistribution)
                .mostAttendedCategory(mostAttendedCategory)
                .favoriteDayOfWeek(favoriteDayOfWeek)
                .favoriteTimeOfDay(favoriteTimeOfDay)
                .averageMeetingSize(averageMeetingSize)
                .mostFrequentRegion(mostFrequentRegion)
                .averageRatingGiven(averageRatingGiven)
                .averageRatingReceivedAsHost(averageRatingReceivedAsHost)
                .build();
    }

    private String getTimeOfDay(int hour) {
        if (hour >= 6 && hour < 12) {
            return "오전";
        } else if (hour >= 12 && hour < 18) {
            return "오후";
        } else if (hour >= 18 && hour < 24) {
            return "저녁";
        } else {
            return "밤";
        }
    }
}
