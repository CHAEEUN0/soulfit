package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MeetingBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_bookmark_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserAuth user;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;


    @Builder
    public MeetingBookmark(Meeting meeting, UserAuth user) {
        this.meeting = meeting;
        this.user = user;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}
