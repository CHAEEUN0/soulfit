package soulfit.soulfit.meeting.dto;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MeetingReviewUpdateRequestDto {


    private Double meetingRating;

    private Double hostRating;

    private String content;

    private List<MultipartFile> images;

}
