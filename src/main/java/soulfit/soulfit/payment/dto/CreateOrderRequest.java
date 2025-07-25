package soulfit.soulfit.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrderRequest {
    private int totalAmount;
    private String orderName;
    private String orderType; // "MEETING" 또는 "SUBSCRIPTION"
    private Long itemId;      // meetingId 또는 subscriptionPlanId
}
