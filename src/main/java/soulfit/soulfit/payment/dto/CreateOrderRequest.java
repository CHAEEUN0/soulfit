package soulfit.soulfit.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrderRequest {
    private int totalAmount;
    private String orderName;
}
