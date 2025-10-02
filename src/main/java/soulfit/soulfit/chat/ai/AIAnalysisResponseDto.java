package soulfit.soulfit.chat.ai;

import lombok.Data;

import java.util.List;

@Data
public class AIAnalysisResponseDto {
    private double positiveScore;
    private double negativeScore;
    private String mood;
    private List<String> keywords;
}
