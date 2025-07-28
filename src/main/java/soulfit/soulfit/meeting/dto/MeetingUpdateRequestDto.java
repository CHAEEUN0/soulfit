package soulfit.soulfit.meeting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Location;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MeetingUpdateRequestDto {


    private String title;

    private String description;

    private Category category;

    private Location location;

    private Integer fee;

    private String feeDescription;

    private List<String> supplies;
    private List<String> schedules;

    private List<MultipartFile> images;

    private List<Long> keywordIds;

    private Boolean canPickup;

    private LocalDateTime meetingTime;

    private Integer duration;

    private LocalDateTime recruitDeadline;

    private MeetingStatus meetingStatus;

    @Min(1)
    private Integer maxParticipants;



}