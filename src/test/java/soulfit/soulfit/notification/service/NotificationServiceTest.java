package soulfit.soulfit.notification.service;


import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.notification.domain.Notification;
import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.dto.NotificationResponse;
import soulfit.soulfit.notification.repository.NotificationRepository;
import soulfit.soulfit.notification.service.NotificationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;

    private UserAuth testUser;

    @BeforeEach
    void setUp() {
        // 테스트 유저 저장
        testUser = new UserAuth();
        testUser.setId(null);
        testUser.setUsername("testuser");
        testUser.setPassword("samplepwd");
        testUser.setEmail("testman@example.con");


        em.persist(testUser);

        // 알림 2개 생성
        notificationRepository.save(Notification.builder()
                .receiver(testUser)
                .type(NotificationType.TYPE_A)
                .title("알림 1")
                .body("본문 1")
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .receiver(testUser)
                .type(NotificationType.TYPE_B)
                .title("알림 2")
                .body("본문 2")
                .isRead(false)
                .build());
    }

    @Test
    void 알림_목록_조회() {
        List<NotificationResponse> result = notificationService.getNotifications(testUser.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).contains("알림");
    }

    @Test
    void 단건_읽음_처리() {
        Notification target = notificationRepository.findAll().get(0);
        assertThat(target.isRead()).isFalse();

        notificationService.markAsRead(target.getId(), testUser.getId());

        Notification updated = notificationRepository.findById(target.getId()).get();
        assertThat(updated.isRead()).isTrue();
    }

    @Test
    void 전체_읽음_처리() {
        notificationService.markAllAsRead(testUser.getId());

        List<Notification> all = notificationRepository.findAll();
        assertThat(all).allMatch(Notification::isRead);
    }

    @Test
    void 단건_삭제() {
        Notification target = notificationRepository.findAll().get(0);
        notificationService.deleteNotification(target.getId(), testUser.getId());

        boolean exists = notificationRepository.existsById(target.getId());
        assertThat(exists).isFalse();
    }
}
