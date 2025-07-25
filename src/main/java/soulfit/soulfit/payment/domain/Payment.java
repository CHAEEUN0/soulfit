package soulfit.soulfit.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentKey;

    // ✅ 모임 주문
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_order_id")
    private MeetingOrder meetingOrder;

    // ✅ 구독 주문
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_order_id")
    private SubscriptionOrder subscriptionOrder;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime approvedAt;

    public enum PaymentMethod {
        CARD("카드"),
        VIRTUAL_ACCOUNT("가상계좌"),
        TRANSFER("계좌이체"),
        MOBILE_PHONE("휴대폰"),
        EASY_PAY("간편결제"),
        OTHER("기타");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public static PaymentMethod fromDescription(String input) {
            for (PaymentMethod method : values()) {
                if (method.name().equalsIgnoreCase(input) || method.description.equals(input)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("Unknown payment method: " + input);
        }
    }

    public enum PaymentStatus {
        SUCCESS, FAILED
    }
}
