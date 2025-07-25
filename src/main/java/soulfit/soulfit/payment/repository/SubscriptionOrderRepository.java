package soulfit.soulfit.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.payment.domain.SubscriptionOrder;

import java.util.Optional;

public interface SubscriptionOrderRepository extends JpaRepository<SubscriptionOrder, Long> {
    Optional<SubscriptionOrder> findByOrderId(String orderId);
}
