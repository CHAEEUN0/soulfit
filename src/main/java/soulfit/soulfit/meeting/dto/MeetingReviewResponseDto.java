package soulfit.soulfit.meeting.dto;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.meeting.domain.MeetingReview;

import java.util.List;

@Builder
@Getter
public class MeetingReviewResponseDto {

    private int meetingRating;

    private int hostRating;

    private String content;

    private List<String> postImageUrls;

    public static  MeetingReviewResponseDto from(MeetingReview meetingReview){
        return MeetingReviewResponseDto.builder()
                .meetingRating(meetingReview.getMeetingRating())
                .hostRating(meetingReview.getHostRating())
                .content(meetingReview.getContent())
                .postImageUrls(meetingReview.getPostImageUrls())
                .build();
    }

}
