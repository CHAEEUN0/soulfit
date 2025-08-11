package soulfit.soulfit.valuestest.dto.ai;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.valuestest.domain.TestType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ValuesTestAnalysisRequestDto {

    private Long surveySubmissionId;
    private Long userId;
    private String testType;
    private List<AnswerItem> answers;
    private LocalDateTime submittedAt;

    @Getter
    @Builder
    public static class AnswerItem {
        private Long questionId;
        private String questionText;
        private Long selectedChoiceId;
        private String selectedChoiceContent;
        private String answerText;
    }
}
