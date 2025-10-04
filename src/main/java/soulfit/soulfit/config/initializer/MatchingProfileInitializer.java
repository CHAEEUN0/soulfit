package soulfit.soulfit.config.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.profile.domain.*;
import soulfit.soulfit.matching.profile.repository.IdealTypeKeywordRepository;
import soulfit.soulfit.matching.profile.repository.MatchingProfileRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@Order(4) // Runs after UserInitializer and ProfileInitializer
@Transactional
public class MatchingProfileInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchingProfileRepository matchingProfileRepository;

    @Autowired
    private IdealTypeKeywordRepository idealTypeKeywordRepository;

    @Override
    public void run(String... args) throws Exception {
        UserAuth regularUser = userRepository.findByUsername("user").orElse(null);
        if (regularUser != null && !matchingProfileRepository.findByUserAuth(regularUser).isPresent()) {
            Set<IdealTypeKeyword> idealTypes1 = createKeywords("운동 좋아함", "유머러스함", "배려심 깊음");
            MatchingProfile matchingProfile1 = MatchingProfile.builder()
                    .userAuth(regularUser)
                    .bio("안녕하세요! 함께 운동하며 즐거운 시간 보내고 싶어요.")
                    .visibility(Visibility.PUBLIC)
                    .job("디자이너")
                    .heightCm(165)
                    .weightKg(55)
                    .religion(Religion.NONE)
                    .smoking(SmokingHabit.NON_SMOKER)
                    .drinking(DrinkingHabit.SOMETIMES)
                    .idealTypes(idealTypes1)
                    .build();
            matchingProfileRepository.save(matchingProfile1);
            System.out.println("✅ Matching profile created for user.");
        }

        UserAuth regularUser2 = userRepository.findByUsername("user2").orElse(null);
        if (regularUser2 != null && !matchingProfileRepository.findByUserAuth(regularUser2).isPresent()) {
            Set<IdealTypeKeyword> idealTypes2 = createKeywords("차분함", "지적임", "운동 좋아함");
            MatchingProfile matchingProfile2 = MatchingProfile.builder()
                    .userAuth(regularUser2)
                    .bio("조용히 운동하는 것을 좋아합니다. 같이 성장할 분 환영해요.")
                    .visibility(Visibility.PUBLIC)
                    .job("개발자")
                    .heightCm(180)
                    .weightKg(75)
                    .religion(Religion.NONE)
                    .smoking(SmokingHabit.NON_SMOKER)
                    .drinking(DrinkingHabit.NEVER)
                    .idealTypes(idealTypes2)
                    .build();
            matchingProfileRepository.save(matchingProfile2);
            System.out.println("✅ Matching profile created for user2.");
        }

        // Additional matching profiles
        for (int i = 3; i <= 12; i++) {
            UserAuth user = userRepository.findByUsername("user" + i).orElse(null);
            if (user != null && !matchingProfileRepository.findByUserAuth(user).isPresent()) {
                String[] jobs = {"학생", "교사", "의사", "엔지니어", "프리랜서", "자영업자", "마케터", "연구원", "공무원", "예술가"};
                Religion[] religions = Religion.values();
                SmokingHabit[] smokingHabits = SmokingHabit.values();
                DrinkingHabit[] drinkingHabits = DrinkingHabit.values();
                String[][] keywordSets = {
                    {"성실함", "책임감 강함"}, {"긍정적임", "사교적임"}, {"독창적임", "예술적 감각"},
                    {"논리적임", "분석적임"}, {"공감 능력 뛰어남", "따뜻함"}, {"모험심 강함", "도전적임"},
                    {"신중함", "계획적임"}, {"유연함", "적응력 좋음"}, {"정직함", "솔직함"}, {"인내심 많음", "끈기 있음"}
                };

                int index = i - 3;
                MatchingProfile profile = MatchingProfile.builder()
                        .userAuth(user)
                        .bio("새로운 사람들과 만나 운동하는 것을 좋아합니다. 잘 부탁드립니다!")
                        .visibility(Visibility.PUBLIC)
                        .job(jobs[index % jobs.length])
                        .heightCm(170 + (i % 15)) // 170-184
                        .weightKg(60 + (i % 20)) // 60-79
                        .religion(religions[index % religions.length])
                        .smoking(smokingHabits[index % smokingHabits.length])
                        .drinking(drinkingHabits[index % drinkingHabits.length])
                        .idealTypes(createKeywords(keywordSets[index % keywordSets.length]))
                        .build();
                matchingProfileRepository.save(profile);
            }
        }
        System.out.println("✅ Additional 10 matching profiles created.");
    }

    private Set<IdealTypeKeyword> createKeywords(String... keywords) {
        return Arrays.stream(keywords)
                .map(keyword -> idealTypeKeywordRepository.findByKeyword(keyword)
                        .orElseGet(() -> idealTypeKeywordRepository.save(IdealTypeKeyword.builder().keyword(keyword).build())))
                .collect(Collectors.toSet());
    }
}