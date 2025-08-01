package soulfit.soulfit.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.meeting.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MeetingRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Category category;

    @NotNull
    private Location location;

    @NotNull
    private Integer fee;
    @NotBlank
    private String feeDescription;

    private List<String> supplies;
    private List<String> schedules;

    private List<MultipartFile> images;

    @NotNull
    private List<Long> keywordIds;

    @NotNull
    private Boolean canPickup;

    @NotNull
    private LocalDateTime meetingTime;

    @NotNull
    private Integer duration;

    @NotNull
    private LocalDateTime recruitDeadline;

    @Min(1)
    @NotNull
    private Integer maxParticipants;



    public Meeting toEntity() {
        return Meeting.builder()
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .location(this.location)
                .fee(this.fee)
                .feeDescription(this.feeDescription)
                .supplies(this.supplies != null ? this.supplies : new ArrayList<>())
                .schedules(this.schedules != null ? this.schedules : new ArrayList<>())
                .canPickup(this.canPickup)
                .meetingTime(this.meetingTime)
                .duration(this.duration)
                .recruitDeadline(this.recruitDeadline)
                .maxParticipants(this.maxParticipants)
                .build();
    }
}