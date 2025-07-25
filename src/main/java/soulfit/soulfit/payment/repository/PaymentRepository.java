package soulfit.soulfit.payment.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.payment.domain.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
}

