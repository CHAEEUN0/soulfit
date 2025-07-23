package soulfit.soulfit.meeting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import soulfit.soulfit.authentication.dto.UserResponseDto;
import soulfit.soulfit.meeting.domain.Location;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Meeting;

import java.time.LocalDateTime;

@Builder
@Data
public class MeetingResponse {

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private Category category;

    @NotNull
    private UserResponseDto host;

    private Location location;

    private LocalDateTime recruitDeadline;

    private int fee;
    private int max_participants;



    public static MeetingResponse from(Meeting meeting) {
        return MeetingResponse.builder()
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .category(meeting.getCategory())
                .host(UserResponseDto.from(meeting.getHost()))
                .location(meeting.getLocation())
                .recruitDeadline(meeting.getRecruitDeadline())
                .fee(meeting.getFee())
                .max_participants(meeting.getMaxParticipants())
                .build();
    }
}
