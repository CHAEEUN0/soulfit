package soulfit.soulfit.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 외부 노출용 주문 ID (UUID)
    @Column(nullable = false, unique = true)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAuth user;

    @Column(nullable = false)
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        this.orderId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }

    public void markAsPaid(LocalDateTime paidAt) {
        this.status = OrderStatus.PAID;
        this.paidAt = paidAt;
    }

    public enum OrderStatus {
        PENDING, PAID, CANCELED
    }
}

