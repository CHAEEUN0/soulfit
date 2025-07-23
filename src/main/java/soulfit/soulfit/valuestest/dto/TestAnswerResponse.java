package soulfit.soulfit.valuestest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import soulfit.soulfit.valuestest.domain.ValueQuestionType;

import java.util.List;

@Data
@AllArgsConstructor
public class TestAnswerResponse {
    private Long questionId;
    private String questionContent;
    private ValueQuestionType questionType;
    private List<ChoiceResponse> choices; // 선택지 목록
    private Long selectedChoiceId; // 사용자가 선택한 선택지 ID
    private String answerText;     // 주관식 답변
}
