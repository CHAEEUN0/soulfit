package soulfit.soulfit.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import soulfit.soulfit.test.domain.QuestionType;

@Data
@AllArgsConstructor
public class TestAnswerResponse {
    private Long questionId;
    private String questionContent;
    private QuestionType questionType;
    private String selectedChoice; // null if TEXT
    private String answerText;     // null if MULTIPLE
}
