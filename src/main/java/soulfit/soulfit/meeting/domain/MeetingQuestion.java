package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingQuestion {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    private String questionText;

    @Enumerated(value = EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "question_order")
    private int order;
}
