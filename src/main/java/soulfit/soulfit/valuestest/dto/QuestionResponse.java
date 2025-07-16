package soulfit.soulfit.valuestest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import soulfit.soulfit.valuestest.domain.Choice;
import soulfit.soulfit.valuestest.domain.ValueQuestionType;
import soulfit.soulfit.valuestest.domain.TestQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class QuestionResponse {
    private Long id;
    private String content;
    private ValueQuestionType type;
    private List<ChoiceDto> choices;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ChoiceDto {
        private Long id;
        private String text;
    }

    // 생성자 (선택형 질문일 경우만 choices 포함)
    public QuestionResponse(TestQuestion question, List<Choice> choices) {
        this.id = question.getId();
        this.content = question.getContent();
        this.type = question.getType();
        this.choices = choices.stream()
                .map(c -> new ChoiceDto(c.getId(), c.getText()))
                .collect(Collectors.toList());
    }

    public QuestionResponse(TestQuestion question) {
        this.id = question.getId();
        this.content = question.getContent();
        this.type = question.getType();
        this.choices = new ArrayList<>();
    }
}

