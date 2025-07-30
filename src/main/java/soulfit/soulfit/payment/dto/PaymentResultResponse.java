package soulfit.soulfit.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import soulfit.soulfit.payment.domain.Payment;

@Getter
@AllArgsConstructor
public class PaymentResultResponse {
    private Payment.PaymentStatus status;
    private String message;
}
