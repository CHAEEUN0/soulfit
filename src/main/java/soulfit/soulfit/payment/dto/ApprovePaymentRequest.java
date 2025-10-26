package soulfit.soulfit.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApprovePaymentRequest {
    private String paymentKey;
    private String orderId;
    private int amount;
}
