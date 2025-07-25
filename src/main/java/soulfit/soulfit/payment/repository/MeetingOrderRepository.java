package soulfit.soulfit.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.payment.domain.MeetingOrder;

import java.util.Optional;

public interface MeetingOrderRepository extends JpaRepository<MeetingOrder, Long> {
    Optional<MeetingOrder> findByOrderId(String orderId);
}
