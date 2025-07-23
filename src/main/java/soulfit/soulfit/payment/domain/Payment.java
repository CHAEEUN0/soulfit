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

    // Toss에서 반환하는 고유 키
    @Column(nullable = false, unique = true)
    private String paymentKey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

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
        EASY_PAY("간편결제"), // 간편결제 추가
        OTHER("기타");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public static PaymentMethod fromDescription(String input) {
            // 1. Enum 상수 이름과 직접 일치하는지 확인 (대소문자 무시)
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.name().equalsIgnoreCase(input)) {
                    return method;
                }
            }

            // 2. 한글 description과 일치하는지 확인
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.description.equals(input)) {
                    return method;
                }
            }

            // 둘 다 해당 없으면 예외 발생
            throw new IllegalArgumentException("Unknown payment method: " + input);
        }
    }

    public enum PaymentStatus {
        SUCCESS, FAILED
    }
}
