package soulfit.soulfit.payment.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.payment.domain.Order;
import soulfit.soulfit.payment.dto.CreateOrderRequest;
import soulfit.soulfit.payment.dto.CreateOrderResponse;
import soulfit.soulfit.payment.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public CreateOrderResponse createOrder(CreateOrderRequest request, UserAuth user) {
        Order order = Order.builder()
                .user(user)
                .totalAmount(request.getTotalAmount())
                .status(Order.OrderStatus.PENDING)
                .build();

        Order saved = orderRepository.save(order);
        return new CreateOrderResponse(saved.getOrderId());
    }

    public Order getValidOrder(String orderId, int requestedAmount) {
        return orderRepository.findByOrderId(orderId)
                .filter(order -> order.getTotalAmount() == requestedAmount)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order or amount mismatch"));
    }

    public void markOrderAsPaid(Order order) {
        order.markAsPaid(java.time.LocalDateTime.now());
        orderRepository.save(order);
    }
}
