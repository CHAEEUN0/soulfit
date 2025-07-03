package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "meeting_member")
public class MeetingMember {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
