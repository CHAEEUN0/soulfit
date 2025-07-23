package soulfit.soulfit.valuestest.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswerRequest {
    private Long sessionId;
    private List<AnswerDto> answers;

    @Data
    public static class AnswerDto {
        private Long questionId;
        private Long selectedChoiceId; // nullable
        private String answerText;     // nullable
    }
}
