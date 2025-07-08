package soulfit.soulfit.meeting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Location;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Meeting;

import java.time.LocalDateTime;

@Data
@Builder
public class MeetingRequest {

    private String title;

    private String description;

    private Category category;

    private Location location;
    private LocalDateTime recruitDeadline;
    private LocalDateTime meetingTime;

    @Min(1)
    private Integer maxParticipants;
    private Integer fee;


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