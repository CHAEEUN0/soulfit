package soulfit.soulfit.valuestest.dto.ai;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.valuestest.domain.TestType;

import java.util.List;

@Getter
@Builder
public class ValuesTestAnalysisRequestDto {

    private Long userId;
    private TestType testType;
    private List<AnswerItem> answers;

    @Getter
    @Builder
    public static class AnswerItem {
        private Long questionId;
        private String questionText;
        private Long selectedChoiceId;
        private String answerText;
    }
}
