package soulfit.soulfit.meeting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.User;

@Data
@Builder
public class MeetingRequest {

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private Category category;

    private int fee;
    private int max_participants;


    public Meeting toEntity(User host) {
        return Meeting.builder()
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .fee(this.fee)
                .capacity(this.max_participants)
                .host(host)
                .build();
    }
}