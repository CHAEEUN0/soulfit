package soulfit.soulfit.meeting.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiResponseDto {
    private List<RecommendationItem> recommendations;

    @Getter
    @NoArgsConstructor
    public static class RecommendationItem {
        private Long meetingId;
        private List<String> reasonKeywords;
    }
}
