package soulfit.soulfit.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.payment.dto.ApprovePaymentRequest;
import soulfit.soulfit.payment.dto.CreateOrderRequest;
import soulfit.soulfit.payment.dto.CreateOrderResponse;
import soulfit.soulfit.payment.dto.PaymentResultResponse;
import soulfit.soulfit.payment.service.OrderService;
import soulfit.soulfit.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * 주문 생성 API
     * @param request 주문 생성 요청 DTO
     * @param user 인증된 사용자 정보
     * @return 생성된 주문 ID
     */
    @PostMapping("/order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserAuth user) {
        CreateOrderResponse response = orderService.createOrder(request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 승인 API
     * @param request 결제 승인 요청 DTO
     * @return 결제 결과
     */
    @PostMapping("/approve")
    public ResponseEntity<PaymentResultResponse> approvePayment(
            @RequestBody ApprovePaymentRequest request) {
        PaymentResultResponse response = paymentService.approvePayment(request);
        return ResponseEntity.ok(response);
    }
}

/*
 * --- Test Commands ---
 *
 * 1. Create Order:
 curl -X POST http://localhost:8080/api/payments/order \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{
"totalAmount": 50000
}'
 *
 *
 * 2. Approve Payment:
 * # Replace {orderId}, {paymentKey}, and {amount} with actual values from the Toss Payments flow.
 * curl -X POST http://localhost:8080/api/payments/approve \
 * -H "Content-Type: application/json" \
 * -H "Authorization: Bearer $TOKEN" \
 * -d '{
 *   "orderId": "your_order_id",
 *   "paymentKey": "your_payment_key",
 *   "amount": 50000
 * }'
 *
 */
