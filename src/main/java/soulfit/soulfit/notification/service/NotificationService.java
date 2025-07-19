package soulfit.soulfit.notification.service;
// NotificationService.java
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.notification.domain.Notification;
import soulfit.soulfit.notification.dto.NotificationResponse;
import soulfit.soulfit.notification.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 1. 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long receiverId) {
        return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 2. 단건 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = findNotificationForUser(notificationId, userId);
        notification.markAsRead();
    }

    // 3. 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long receiverId) {
        List<Notification> unreadList = notificationRepository.findByReceiverIdAndIsReadFalse(receiverId);
        unreadList.forEach(Notification::markAsRead);
    }

    // 4. 단건 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = findNotificationForUser(notificationId, userId);
        notificationRepository.delete(notification);
    }

    private Notification findNotificationForUser(Long notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getReceiver().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("알림이 존재하지 않거나 권한이 없습니다."));
    }

    private NotificationResponse toDto(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .body(notification.getBody())
                .targetId(notification.getTargetId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt().toString())
                .build();
    }
}

