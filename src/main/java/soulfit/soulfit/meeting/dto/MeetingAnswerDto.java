package soulfit.soulfit.meeting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soulfit.soulfit.meeting.domain.MeetingAnswer;
import soulfit.soulfit.meeting.domain.QuestionType;

import java.util.List;

@Getter
@Setter
public class MeetingAnswerDto {

    @Getter
    @NoArgsConstructor
    public static class Request {
        private List<AnswerItem> answers;
    }

    @Getter
    @NoArgsConstructor
    public static class AnswerItem {
        private Long questionId;
        private String textAnswer;
        private List<String> selectedChoices;
    }

    @Getter
    public static class Response {
        private final Long participantId;
        private final Long userId;
        private final String username;
        private final List<AnswerResponseItem> answers;

        public Response(Long participantId, Long userId, String username, List<AnswerResponseItem> answers) {
            this.participantId = participantId;
            this.userId = userId;
            this.username = username;
            this.answers = answers;
        }
    }

    @Getter
    public static class AnswerResponseItem {
        private final Long questionId;
        private final QuestionType questionType;
        private final String textAnswer;
        private final List<String> selectedChoices;

        public AnswerResponseItem(MeetingAnswer answer) {
            this.questionId = answer.getMeetingQuestion().getId();
            this.questionType = answer.getMeetingQuestion().getQuestionType();
            this.textAnswer = answer.getTextAnswer();
            this.selectedChoices = answer.getSelectedChoices();
        }
    }
}
