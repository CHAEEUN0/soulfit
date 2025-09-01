package soulfit.soulfit.matching.conversation.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "conversation_request", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_conversation_request_from_to_pending",
                columnNames = {"from_user_id", "to_user_id", "status"}
        )
})
public class ConversationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private UserAuth fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private UserAuth toUser;

    @Column(nullable = false, length = 255)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RequestStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private Long chatRoomId;

    @Builder
    public ConversationRequest(UserAuth fromUser, UserAuth toUser, String message) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.message = message;
        this.status = RequestStatus.PENDING;
    }

    public void updateStatus(RequestStatus status) {
        this.status = status;
    }
}