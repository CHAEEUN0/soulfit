package soulfit.soulfit.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.payment.domain.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
}
