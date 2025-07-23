package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingQuestion {

    @Id @GeneratedValue
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    private String questionText;

    @Enumerated(value = EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "question_order")
    private int order;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "meeting_question_choice", joinColumns = @JoinColumn(name = "meeting_question_id"))
    @Column(name = "choice")
    private List<String> choices = new ArrayList<>();

    public static MeetingQuestion createMeetingQuestion(String questionText, QuestionType questionType, int order, List<String> choices) {
        MeetingQuestion question = new MeetingQuestion();
        question.questionText = questionText;
        question.questionType = questionType;
        question.order = order;
        if (choices != null) {
            question.choices.addAll(choices);
        }
        return question;
    }

}