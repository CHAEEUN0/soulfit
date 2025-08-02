package soulfit.soulfit.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받을 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserAuth receiver;

    // 알림을 발생시킨 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserAuth sender;

    // 알림 유형 (ENUM)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false)
    private String body;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long id, UserAuth receiver, UserAuth sender, NotificationType type, String title, String body, Long targetId, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.receiver = receiver;
        this.sender = sender;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetId = targetId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 읽음 처리
    public void markAsRead() {
        this.isRead = true;
    }
}
