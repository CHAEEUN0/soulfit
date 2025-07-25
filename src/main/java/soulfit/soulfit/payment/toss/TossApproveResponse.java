package soulfit.soulfit.payment.toss;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossApproveResponse {
    private String paymentKey;
    private String orderId;
    private int amount;
    private String method; // 예: "CARD", "VIRTUAL_ACCOUNT"
    private ZonedDateTime approvedAt;
}

