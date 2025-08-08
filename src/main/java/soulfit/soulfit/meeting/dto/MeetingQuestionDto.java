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
        private String questionText;

        public Request(String questionText) {
            this.questionText = questionText;
        }
    }

    @Getter
    public static class Response {
        private final Long id;
        private final String questionText;

        public Response(MeetingQuestion question) {
            this.id = question.getId();
            this.questionText = question.getQuestionText();
        }
    }
}
