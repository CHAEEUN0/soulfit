
package soulfit.soulfit.meeting.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MeetingStatsResponseDto {
    private long totalMeetingsAttended;
    private Map<String, Long> monthlyActivity;
    private Map<String, Long> categoryDistribution;
    private String mostAttendedCategory;

    // 추가된 통계
    private String favoriteDayOfWeek;
    private String favoriteTimeOfDay;
    private double averageMeetingSize;
    private String mostFrequentRegion;
    private double averageRatingGiven;
    private double averageRatingReceivedAsHost;
}
