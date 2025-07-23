package soulfit.soulfit.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.payment.domain.Order;
import soulfit.soulfit.payment.domain.Payment;
import soulfit.soulfit.payment.dto.ApprovePaymentRequest;
import soulfit.soulfit.payment.dto.PaymentResultResponse;
import soulfit.soulfit.payment.repository.PaymentRepository;
import soulfit.soulfit.payment.toss.TossApproveResponse;
import soulfit.soulfit.payment.toss.TossPaymentsClient;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final TossPaymentsClient tossClient;

    @Transactional
    public PaymentResultResponse approvePayment(ApprovePaymentRequest request) {
        // 1. 주문 검증
        Order order = orderService.getValidOrder(request.getOrderId(), request.getAmount());

        // 2. Toss에 결제 승인 요청
        TossApproveResponse tossResponse = tossClient.approvePayment(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount()
        );

        // 3. Payment 엔티티 저장
        Payment payment = Payment.builder()
                .paymentKey(request.getPaymentKey())
                .order(order)
                .amount(request.getAmount())
                .method(Payment.PaymentMethod.fromDescription(tossResponse.getMethod()))
                .status(Payment.PaymentStatus.SUCCESS)
                .approvedAt(tossResponse.getApprovedAt().toLocalDateTime())
                .build();
        paymentRepository.save(payment);

        // 4. 주문 상태 변경
        orderService.markOrderAsPaid(order);

        return new PaymentResultResponse(Payment.PaymentStatus.SUCCESS, "결제가 완료되었습니다.");
    }
}

