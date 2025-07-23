package soulfit.soulfit.payment.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.payment.domain.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
}
