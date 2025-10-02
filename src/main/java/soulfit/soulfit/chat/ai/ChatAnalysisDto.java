package soulfit.soulfit.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAnalysisDto {
    private double positiveScore;
    private double negativeScore;
    private String mood;
    private List<String> keywords;

    public static ChatAnalysisDto from(AIAnalysisResponseDto response) {
        return ChatAnalysisDto.builder()
                .positiveScore(response.getPositiveScore())
                .negativeScore(response.getNegativeScore())
                .mood(response.getMood())
                .keywords(response.getKeywords())
                .build();
    }
}
