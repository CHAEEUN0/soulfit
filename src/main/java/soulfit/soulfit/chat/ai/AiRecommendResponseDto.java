package soulfit.soulfit.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendResponseDto {
    private List<RecommendationData> recommendations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RecommendationData {
        private String text;
        private Integer score;
    }
}
