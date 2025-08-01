package soulfit.soulfit.meeting.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingReview;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MeetingReviewRequestDto {

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @NotNull
    private Double meetingRating;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @NotNull
    private Double hostRating;

    private String content;

    private List<MultipartFile> images;

    public MeetingReview toEntity(Meeting meeting, UserAuth user){

        return MeetingReview.builder()
                .meeting(meeting)
                .user(user)
                .meetingRating(meetingRating)
                .hostRating(hostRating)
                .content(content)
                .build();


    }

}
