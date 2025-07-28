package soulfit.soulfit.meeting.dto;


import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Getter
public class MeetingReviewUpdateRequestDto {


    private int meetingRating;

    private int hostRating;

    private String content;

    private List<MultipartFile> images;

}
