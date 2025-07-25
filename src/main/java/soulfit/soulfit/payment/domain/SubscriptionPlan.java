package soulfit.soulfit.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: BASIC, PREMIUM, VIP 등
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int price;

    // 구독 기간 (예: 월 단위, 30일 등)
    @Column(nullable = false)
    private int durationDays;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
