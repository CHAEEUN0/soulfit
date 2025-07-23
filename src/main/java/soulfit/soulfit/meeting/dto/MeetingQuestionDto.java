package soulfit.soulfit.meeting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import soulfit.soulfit.meeting.domain.MeetingQuestion;
import soulfit.soulfit.meeting.domain.QuestionType;

import java.util.List;

public class MeetingQuestionDto {

    @Getter
    @NoArgsConstructor
    public static class Request {
        private List<QuestionItem> questions;

        public Request(List<QuestionItem> questions) {
            this.questions = questions;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class QuestionItem {
        private String questionText;
        private QuestionType questionType;
        private int order;
        private List<String> choices;

        public QuestionItem(String questionText, QuestionType questionType, int order, List<String> choices) {
            this.questionText = questionText;
            this.questionType = questionType;
            this.order = order;
            this.choices = choices;
        }

        public MeetingQuestion toEntity() {
            return MeetingQuestion.createMeetingQuestion(questionText, questionType, order, choices);
        }
    }

    @Getter
    public static class Response {
        private final Long id;
        private final String questionText;
        private final QuestionType questionType;
        private final int order;
        private final List<String> choices;

        public Response(MeetingQuestion question) {
            this.id = question.getId();
            this.questionText = question.getQuestionText();
            this.questionType = question.getQuestionType();
            this.order = question.getOrder();
            this.choices = question.getChoices();
        }
    }
}
