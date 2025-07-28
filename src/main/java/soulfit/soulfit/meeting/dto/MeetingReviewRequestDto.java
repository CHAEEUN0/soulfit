package soulfit.soulfit.meeting.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingReview;

import java.util.List;

@Getter
public class MeetingReviewRequestDto {

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private int meetingRating;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private int hostRating;

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
