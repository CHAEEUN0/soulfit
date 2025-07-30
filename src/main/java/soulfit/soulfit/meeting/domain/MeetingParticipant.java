package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAuth user;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDateTime joinedAt;
    private LocalDateTime approvedAt;
    private String rejectedReason;
    private boolean reviewed;

    public void approve() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Participant status is not PENDING. Cannot approve.");
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }
}
