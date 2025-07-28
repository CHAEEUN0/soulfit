package soulfit.soulfit.meeting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MeetingRequest {

    private String title;

    private String description;

    private Category category;

    private Location location;

    private Integer fee;
    private String feeDescription;


    private LocalDateTime recruitDeadline;
    private LocalDateTime meetingTime;

    @Min(1)
    private Integer maxParticipants;



    //준비물추가
    private List<MultipartFile> images;
    private List<MeetingSchedule> schedules;
    private boolean canPickup;


    public Meeting toEntity() {
        return Meeting.builder()
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .location(this.location)
                .recruitDeadline(this.recruitDeadline)
                .maxParticipants(this.maxParticipants)
                .fee(this.fee)
                .build();
    }
}