package soulfit.soulfit.meeting.dto;

import lombok.Builder;
import lombok.Data;
import soulfit.soulfit.meeting.domain.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class MeetingResponseDto {

    private Long id;
    private String title;
    private String description;
    private Category category;
    private Location location;

    private int fee;
    private String feeDescription;

    private List<String> supplies;
    private List<String> schedules;
    private List<String> imageUrls;
    private List<String> keywords;


    private boolean canPickup;

    private LocalDateTime meetingTime;
    private Integer duration;

    private LocalDateTime recruitDeadline;
    private int maxParticipants;

    private MeetingStatus status;

    private int currentParticipants;

    private LocalDateTime createdAt;

    //호스트 정보, 리뷰 평균평점, 리뷰 추가



    public static MeetingResponseDto from(Meeting meeting) {
        return MeetingResponseDto.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .category(meeting.getCategory())
                .location(meeting.getLocation())
                .fee(meeting.getFee())
                .feeDescription(meeting.getFeeDescription())
                .supplies(meeting.getSupplies())
                .schedules(meeting.getSchedules())
                .imageUrls(meeting.getImages()
                                .stream()
                                .map(MeetingImage::getImageUrl)
                                .toList())
                .keywords(meeting.getKeywords()
                                .stream()
                                .map(Keyword::getName)
                                .toList())
                .canPickup(meeting.isCanPickup())
                .meetingTime(meeting.getMeetingTime())
                .duration(meeting.getDuration())
                .recruitDeadline(meeting.getRecruitDeadline())
                .maxParticipants(meeting.getMaxParticipants())
                .status(meeting.getMeetingStatus())
                .currentParticipants(meeting.getCurrentParticipants())
                .createdAt(meeting.getCreatedAt())
                .build();
    }
}
