package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "meeting_participant")
@NoArgsConstructor
public class MeetingParticipant {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserAuth user;

    private Approvalstatus approval_status;

    private LocalDateTime joined_at;
    private LocalDateTime approved_at;
    private String rejected_reason;
    private boolean reviewed;

    public void approve() {
        if (this.approval_status != Approvalstatus.PENDING) {
            throw new IllegalStateException("Participant status is not PENDING. Cannot approve.");
        }
        this.approval_status = Approvalstatus.APPROVED;
        this.approved_at = LocalDateTime.now();
    }
}
