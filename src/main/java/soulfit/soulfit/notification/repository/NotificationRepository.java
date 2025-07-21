package soulfit.soulfit.notification.repository;
// NotificationRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.notification.domain.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    List<Notification> findByReceiverIdAndIsReadFalse(Long receiverId);
}

