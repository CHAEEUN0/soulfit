package soulfit.soulfit.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.payment.domain.MeetingOrder;
import soulfit.soulfit.payment.domain.Payment;
import soulfit.soulfit.payment.domain.SubscriptionOrder;
import soulfit.soulfit.payment.dto.ApprovePaymentRequest;
import soulfit.soulfit.payment.dto.PaymentResultResponse;
import soulfit.soulfit.payment.repository.MeetingOrderRepository;
import soulfit.soulfit.payment.repository.PaymentRepository;
import soulfit.soulfit.payment.repository.SubscriptionOrderRepository;
import soulfit.soulfit.payment.toss.TossApproveResponse;
import soulfit.soulfit.payment.toss.TossPaymentsClient;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MeetingOrderRepository meetingOrderRepository;
    private final SubscriptionOrderRepository subscriptionOrderRepository;
    private final TossPaymentsClient tossClient;

    @Transactional
    public PaymentResultResponse approvePayment(ApprovePaymentRequest request) {
        TossApproveResponse tossResponse = tossClient.approvePayment(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount()
        );

        MeetingOrder meetingOrder = meetingOrderRepository.findByOrderId(request.getOrderId()).orElse(null);
        SubscriptionOrder subscriptionOrder = null;
        if (meetingOrder == null) {
            subscriptionOrder = subscriptionOrderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));
        }

        if (meetingOrder != null) {
            if (meetingOrder.getTotalAmount() != request.getAmount()) throw new IllegalArgumentException("Amount mismatch");
            meetingOrder.markAsPaid(java.time.LocalDateTime.now());
            meetingOrderRepository.save(meetingOrder);
        } else {
            if (subscriptionOrder.getTotalAmount() != request.getAmount()) throw new IllegalArgumentException("Amount mismatch");
            subscriptionOrder.markAsPaid(java.time.LocalDateTime.now());
            subscriptionOrderRepository.save(subscriptionOrder);
        }

        Payment payment = Payment.builder()
                .paymentKey(request.getPaymentKey())
                .meetingOrder(meetingOrder)
                .subscriptionOrder(subscriptionOrder)
                .amount(request.getAmount())
                .method(Payment.PaymentMethod.fromDescription(tossResponse.getMethod()))
                .status(Payment.PaymentStatus.SUCCESS)
                .approvedAt(tossResponse.getApprovedAt().toLocalDateTime())
                .build();
        paymentRepository.save(payment);

        return new PaymentResultResponse(Payment.PaymentStatus.SUCCESS, "결제가 완료되었습니다.");
    }
}

