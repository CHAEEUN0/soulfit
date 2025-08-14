package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public static MeetingQuestion create(String questionText) {
        MeetingQuestion question = new MeetingQuestion();
        question.questionText = questionText;
        return question;
    }
}