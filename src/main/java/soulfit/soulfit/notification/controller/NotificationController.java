package soulfit.soulfit.notification.controller;
// NotificationController.java
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.notification.dto.NotificationResponse;
import soulfit.soulfit.notification.service.NotificationService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // [1] 알림 목록 조회
    @GetMapping
    public List<NotificationResponse> getNotifications(@AuthenticationPrincipal UserAuth user) {
        return notificationService.getNotifications(user.getId());
    }

    // [2] 단건 읽음 처리
    @PostMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId,
                           @AuthenticationPrincipal UserAuth user) {
        notificationService.markAsRead(notificationId, user.getId());
    }

    // [3] 전체 읽음 처리
    @PostMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal UserAuth user) {
        notificationService.markAllAsRead(user.getId());
    }

    // [4] 단건 알림 삭제
    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable Long notificationId,
                                   @AuthenticationPrincipal UserAuth user) {
        notificationService.deleteNotification(notificationId, user.getId());
    }
}


