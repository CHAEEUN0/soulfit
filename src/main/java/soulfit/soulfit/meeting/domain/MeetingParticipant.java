package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "meeting_participant")
public class MeetingParticipant {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Approvalstatus approval_status;

}
