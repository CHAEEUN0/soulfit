package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingAnswer {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private MeetingParticipant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "question_id")
    private MeetingQuestion meetingQuestion;

    // 주관식 답변
    private String textAnswer;

    // 객관식 답변 (중복 선택 가능)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "meeting_answer_choice", 
                     joinColumns = @JoinColumn(name = "meeting_answer_id"))
    @Column(name = "selected_choice")
    private List<String> selectedChoices = new ArrayList<>();

    public static MeetingAnswer createTextAnswer(MeetingParticipant participant, MeetingQuestion question, String textAnswer) {
        MeetingAnswer answer = new MeetingAnswer();
        answer.participant = participant;
        answer.meetingQuestion = question;
        answer.textAnswer = textAnswer;
        return answer;
    }

    public static MeetingAnswer createChoiceAnswer(MeetingParticipant participant, MeetingQuestion question, List<String> selectedChoices) {
        MeetingAnswer answer = new MeetingAnswer();
        answer.participant = participant;
        answer.meetingQuestion = question;
        answer.selectedChoices.addAll(selectedChoices);
        return answer;
    }
}