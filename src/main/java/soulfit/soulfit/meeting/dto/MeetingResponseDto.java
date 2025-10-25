package soulfit.soulfit.meeting.dto;

import lombok.Builder;
import lombok.Data;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.MeetingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Data
public class MeetingResponseDto {

    private Long id;
    private String title;
    private String description;
    private Category category;

    // Host
    private String hostName;
    private String hostProfileImageUrl;

    // Images & Keywords
    private List<String> imageUrls;
    private List<String> keywords;

    // D-day
    private String ddayBadge;

    // Schedule & Location
    private List<String> schedules;
    private String fullAddress; // meetPlaceAddress, venuePlaceAddress 통합
    private boolean canPickup;
    private LocalDateTime meetingTime;
    private Integer duration;
    private LocalDateTime recruitDeadline;

    // Participants
    private int maxParticipants;
    private int currentParticipants;
    private ParticipantStatsDto participantStats;

    // Reviews
    private int reviewCount;
    private double reviewAvg;
    private String reviewSummary;
    private List<MeetingReviewResponseDto> reviews;

    // Supplies & Price
    private List<String> supplies;
    private int pricePerPerson; // fee -> pricePerPerson
    private String feeDescription;

    // Status & Timestamps
    private MeetingStatus status;
    private LocalDateTime createdAt;

    // AI Recommendation
    private List<String> recommendationReasons;
}
