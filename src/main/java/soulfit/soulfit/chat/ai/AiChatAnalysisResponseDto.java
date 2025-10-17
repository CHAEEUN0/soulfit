package soulfit.soulfit.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatAnalysisResponseDto {

    private Map<String, List<String>> personality;
    private Map<String, String> empathy;
    private Map<String, String> responseSpeed;
    private Map<String, String> questionFrequency;
    private String interestLevel;
    private VibeDto vibe;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VibeDto {
        private List<String> keywords;
        private String summary;
    }

}
