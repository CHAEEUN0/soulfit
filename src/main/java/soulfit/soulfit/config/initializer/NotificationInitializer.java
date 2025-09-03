package soulfit.soulfit.config.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.repository.NotificationRepository;
import soulfit.soulfit.notification.service.NotificationService;

@Component
@Profile("!test")
@Order(5) // 다른 Initializer들(User, Profile 등)이 실행된 후 마지막에 실행되도록 순서를 높게 설정
public class NotificationInitializer implements CommandLineRunner {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 알림 데이터가 있으면 생성하지 않음 (멱등성 보장)
        if (notificationRepository.count() > 0) {
            return;
        }

        // 1. 알림을 주고받을 사용자 조회
        UserAuth user = userRepository.findByUsername("user").orElseThrow(
            () -> new RuntimeException("User 'user' not found. Please run UserInitializer first.")
        );
        UserAuth admin = userRepository.findByUsername("admin").orElseThrow(
            () -> new RuntimeException("User 'admin' not found. Please run UserInitializer first.")
        );
        UserAuth user2 = userRepository.findByUsername("user2").orElseThrow(
            () -> new RuntimeException("User 'user2' not found. Please run UserInitializer first.")
        );

        // 2. NotificationService를 사용하여 샘플 알림 생성
        // 예시 1: user2가 user에게 '좋아요' 알림
        notificationService.sendNotification(
                user2,
                user,
                NotificationType.LIKE,
                "새로운 좋아요",
                "user2님이 회원님의 프로필을 좋아합니다.",
                user2.getId() // targetId는 예시로 프로필을 누른 사용자의 ID를 사용
        );

        // 예시 2: admin이 user에게 '댓글' 알림
        notificationService.sendNotification(
                admin,
                user,
                NotificationType.COMMENT,
                "새로운 댓글",
                "admin님이 회원님의 게시물에 댓글을 남겼습니다.",
                1L // targetId는 예시로 게시물 ID 1을 사용
        );

        // 예시 3: user가 admin에게 '모임 참여' 알림
        notificationService.sendNotification(
                user,
                admin,
                NotificationType.JOIN_MEETING,
                "새로운 모임 참여 요청",
                "user님이 'Sample Fitness Meeting' 모임에 참여를 신청했습니다.",
                1L // targetId는 예시로 모임 ID 1을 사용
        );

        System.out.println("✅ Sample notifications created.");
    }
}
