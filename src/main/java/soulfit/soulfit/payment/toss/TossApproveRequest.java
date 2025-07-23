package soulfit.soulfit.payment.toss;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossApproveRequest {
    private String paymentKey;
    private String orderId;
    private int amount;
}

