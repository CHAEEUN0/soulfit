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
            UserAuth regularUser2 = userRepository.findByUsername("user2").orElseThrow();

            // adminUser의 프로필 생성 (nickname 필드 없이 현재 UserProfile 엔티티에 맞춰서)
            UserProfile adminProfile = new UserProfile(
                adminUser,
                LocalDate.of(1985, 5, 15), // 예시 생년월일
                Gender.MALE, // 예시 성별
                MbtiType.ISTJ, // 예시 MBTI
                "https://picsum.photos/200/300?random=1", // 예시 프로필 이미지 URL
                "Soulfit 관리자입니다. 시스템 운영 및 사용자 지원을 담당합니다."
            );
            adminProfile.setRegion("서울 강남구");
            adminProfile.setLatitude(37.4979);
            adminProfile.setLongitude(127.0276);
            userProfileRepository.save(adminProfile);

            // regularUser의 프로필 생성 (nickname 필드 없이 현재 UserProfile 엔티티에 맞춰서)
            UserProfile userProfile = new UserProfile(
                regularUser,
                LocalDate.of(1992, 11, 22), // 예시 생년월일
                Gender.FEMALE, // 예시 성별
                MbtiType.ENFP, // 예시 MBTI
                "https://picsum.photos/200/300?random=2", // 예시 프로필 이미지 URL
                "운동을 사랑하는 일반 사용자입니다. 새로운 운동 친구를 찾고 있어요!"
            );
            userProfile.setRegion("서울 마포구");
            userProfile.setLatitude(37.56629);
            userProfile.setLongitude(126.9014);
            userProfileRepository.save(userProfile);

            // regularUser2의 프로필 생성
            UserProfile userProfile2 = new UserProfile(
                regularUser2,
                LocalDate.of(1995, 8, 20), // 예시 생년월일
                Gender.MALE, // 예시 성별
                MbtiType.INTP, // 예시 MBTI
                "https://picsum.photos/200/300?random=3", // 예시 프로필 이미지 URL
                "조용히 운동하는 것을 즐깁니다. 같이 성장할 분 환영합니다."
            );
            userProfile2.setRegion("서울 관악구");
            userProfile2.setLatitude(37.4784);
            userProfile2.setLongitude(126.9517);
            userProfileRepository.save(userProfile2);

            // Additional user profiles
            for (int i = 3; i <= 12; i++) {
                UserAuth user = userRepository.findByUsername("user" + i).orElse(null);
                if (user != null && !userProfileRepository.findByUserAuth(user).isPresent()) {
                    Gender gender = (i % 2 == 0) ? Gender.FEMALE : Gender.MALE;
                    MbtiType[] mbtiTypes = MbtiType.values();
                    MbtiType mbti = mbtiTypes[(i - 3) % mbtiTypes.length];

                    String[] regions = {"서울 용산구", "서울 성동구", "서울 광진구", "서울 동대문구", "서울 중랑구", "서울 성북구", "서울 강북구", "서울 도봉구", "서울 노원구", "서울 은평구"};
                    double[] latitudes = {37.5326, 37.5635, 37.5385, 37.5744, 37.6066, 37.5895, 37.6396, 37.6688, 37.6542, 37.6028};
                    double[] longitudes = {126.9900, 127.0368, 127.0823, 127.0397, 127.0932, 127.0167, 127.0257, 127.0471, 127.0618, 126.9291};
                    int regionIndex = (i - 3) % regions.length;

                    UserProfile profile = new UserProfile(
                        user,
                        LocalDate.of(1990 + (i % 10), (i % 12) + 1, (i % 28) + 1),
                        gender,
                        mbti,
                        "https://picsum.photos/200/300?random=" + (i + 3),
                        "안녕하세요! user" + i + "입니다. 함께 즐겁게 운동하고 싶어요."
                    );
                    profile.setRegion(regions[regionIndex]);
                    profile.setLatitude(latitudes[regionIndex]);
                    profile.setLongitude(longitudes[regionIndex]);
                    userProfileRepository.save(profile);
                }
            }

            System.out.println("✅ User profiles created (without nickname field).");
        }
    }
}