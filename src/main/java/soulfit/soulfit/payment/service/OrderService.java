package soulfit.soulfit.payment.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.payment.domain.MeetingOrder;
import soulfit.soulfit.payment.domain.SubscriptionOrder;
import soulfit.soulfit.payment.domain.SubscriptionPlan;
import soulfit.soulfit.payment.dto.CreateOrderRequest;
import soulfit.soulfit.payment.dto.CreateOrderResponse;
import soulfit.soulfit.payment.repository.MeetingOrderRepository;
import soulfit.soulfit.payment.repository.SubscriptionOrderRepository;
import soulfit.soulfit.payment.repository.SubscriptionPlanRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MeetingOrderRepository meetingOrderRepository;
    private final SubscriptionOrderRepository subscriptionOrderRepository;
    private final MeetingRepository meetingRepository; // Assuming this exists
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public CreateOrderResponse createOrder(CreateOrderRequest request, UserAuth user) {
        String orderId;
        if ("MEETING".equals(request.getOrderType())) {
            Meeting meeting = meetingRepository.findById(request.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid meeting ID"));

            MeetingOrder meetingOrder = MeetingOrder.builder()
                    .user(user)
                    .meeting(meeting)
                    .totalAmount(request.getTotalAmount())
                    .status(MeetingOrder.OrderStatus.PENDING)
                    .build();
            orderId = meetingOrderRepository.save(meetingOrder).getOrderId();

        } else if ("SUBSCRIPTION".equals(request.getOrderType())) {
            SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID"));

            SubscriptionOrder subscriptionOrder = SubscriptionOrder.builder()
                    .user(user)
                    .plan(plan)
                    .totalAmount(request.getTotalAmount())
                    .status(SubscriptionOrder.OrderStatus.PENDING)
                    .build();
            orderId = subscriptionOrderRepository.save(subscriptionOrder).getOrderId();
        } else {
            throw new IllegalArgumentException("Invalid order type");
        }
        return new CreateOrderResponse(orderId);
    }
}
