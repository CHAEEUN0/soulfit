package soulfit.soulfit.meeting.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiReviewRequestDto {
    private List<ReviewData> reviews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewData {
        private String content;
        private Integer rating;
        private String createdAt; // ISO 8601 format
    }
}
