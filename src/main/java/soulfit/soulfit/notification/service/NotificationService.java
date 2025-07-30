package soulfit.soulfit.notification.service;
// NotificationService.java
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.notification.domain.Notification;
import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.dto.NotificationResponse;
import soulfit.soulfit.notification.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final MeetingRepository meetingRepository;

    Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // 1. 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long receiverId) {
        logger.info("Fetching notifications for receiverId: {}", receiverId);
        List<Notification> notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId);
        logger.info("Found {} notifications.", notifications.size());

        Set<Long> senderIds = notifications.stream()
                .filter(n -> n.getSender() != null)
                .map(n -> n.getSender().getId())
                .collect(Collectors.toSet());
        logger.info("Extracted senderIds for thumbnail lookup: {}", senderIds);

        Set<Long> meetingIds = notifications.stream()
                .filter(n -> n.getType() == NotificationType.APPROVED)
                .map(Notification::getTargetId)
                .collect(Collectors.toSet());
        logger.info("Extracted meetingIds for thumbnail lookup: {}", meetingIds);

        Map<Long, UserAuth> userMap = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(UserAuth::getId, Function.identity()));
        logger.info("Fetched {} user profiles for thumbnails.", userMap.size());

        Map<Long, Meeting> meetingMap = meetingRepository.findAllById(meetingIds).stream()
                .collect(Collectors.toMap(Meeting::getId, Function.identity()));
        logger.info("Fetched {} meetings for thumbnails.", meetingMap.size());

        List<NotificationResponse> response = notifications.stream()
                .map(notification -> toDto(notification, userMap, meetingMap))
                .collect(Collectors.toList());
        logger.info("Successfully mapped {} notifications to DTOs.", response.size());
        return response;
    }

    // 2. 단건 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = findNotificationForUser(notificationId, userId);
        notification.markAsRead();

        logger.info("Notification "+notificationId +" marked as read");
    }

    // 3. 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long receiverId) {
        List<Notification> unreadList = notificationRepository.findByReceiverIdAndIsReadFalse(receiverId);
        unreadList.forEach(Notification::markAsRead);

        logger.info("All Notifications marked as read, size : ("+unreadList.size()+")");
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

    private NotificationResponse toDto(Notification notification, Map<Long, UserAuth> userMap, Map<Long, Meeting> meetingMap) {
        String thumbnailUrl = null;

        switch (notification.getType()) {
            case LIKE:
            case CONVERSATION_REQUEST:
            case LIKE_POST:
            case COMMENT:
            case JOIN_MEETING:
            case JOIN_CHAT:
                if (notification.getSender() != null) {
                    UserAuth sender = userMap.get(notification.getSender().getId());
                    if (sender != null && sender.getUserProfile() != null) {
                        thumbnailUrl = sender.getUserProfile().getProfileImageUrl();
                    }
                }
                break;
            case APPROVED:
                Meeting meeting = meetingMap.get(notification.getTargetId());
                if (meeting != null) {
                    thumbnailUrl = "err.jpg"; //TODO : get meeting thumbnail (after Meeting.java fixed)
                }
                break;
            default:
                break;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .body(notification.getBody())
                .targetId(notification.getTargetId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt().toString())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    @Transactional
    public void sendNotification(UserAuth sender, UserAuth receiver, NotificationType type, String title, String body, Long targetId) {
        if (receiver == null) {
            throw new RuntimeException("수신자를 찾을 수 없습니다.");
        }

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)
                .title(title)
                .body(body)
                .targetId(targetId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }
}

