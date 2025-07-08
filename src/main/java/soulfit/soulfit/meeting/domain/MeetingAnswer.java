package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingAnswer {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private MeetingParticipant participant;

    @OneToOne
    @JoinColumn(name= "question_id")
    private MeetingQuestion meetingQuestion;

    private String answer_text;
}
