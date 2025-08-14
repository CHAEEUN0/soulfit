package soulfit.soulfit.config.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.MbtiType;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.repository.UserProfileRepository;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

@Component
@Profile("!test")
@Order(3) // Runs after UserInitializer and MeetingDataInitializer
@Transactional
public class ProfileInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userProfileRepository.count() == 0) {
            UserAuth adminUser = userRepository.findByUsername("admin").orElseThrow();
            UserAuth regularUser = userRepository.findByUsername("user").orElseThrow();

            // adminUser의 프로필 생성 (nickname 필드 없이 현재 UserProfile 엔티티에 맞춰서)
            UserProfile adminProfile = new UserProfile(
                adminUser,
                LocalDate.of(1985, 5, 15), // 예시 생년월일
                Gender.MALE, // 예시 성별
                MbtiType.ISTJ, // 예시 MBTI
                "https://example.com/admin_profile.jpg", // 예시 프로필 이미지 URL
                "Soulfit 관리자입니다. 시스템 운영 및 사용자 지원을 담당합니다."
            );
            userProfileRepository.save(adminProfile);

            // regularUser의 프로필 생성 (nickname 필드 없이 현재 UserProfile 엔티티에 맞춰서)
            UserProfile userProfile = new UserProfile(
                regularUser,
                LocalDate.of(1992, 11, 22), // 예시 생년월일
                Gender.FEMALE, // 예시 성별
                MbtiType.ENFP, // 예시 MBTI
                "https://example.com/user_profile.jpg", // 예시 프로필 이미지 URL
                "운동을 사랑하는 일반 사용자입니다. 새로운 운동 친구를 찾고 있어요!"
            );
            userProfileRepository.save(userProfile);

            System.out.println("✅ User profiles created (without nickname field).");
        }
    }
}