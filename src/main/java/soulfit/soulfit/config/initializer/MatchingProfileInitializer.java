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
        // If matching profiles already exist, do nothing.
        if (matchingProfileRepository.count() > 0) {
            return;
        }

        UserAuth regularUser = userRepository.findByUsername("user").orElse(null);
        UserAuth regularUser2 = userRepository.findByUsername("user2").orElse(null);

        if (regularUser == null || regularUser2 == null) {
            System.out.println("Skipping MatchingProfileInitializer: user or user2 not found.");
            return;
        }

        // Create and save some ideal type keywords
        Set<IdealTypeKeyword> idealTypes1 = createKeywords("운동 좋아함", "유머러스함", "배려심 깊음");
        Set<IdealTypeKeyword> idealTypes2 = createKeywords("차분함", "지적임", "운동 좋아함");

        // Create MatchingProfile for 'user'
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

        // Create MatchingProfile for 'user2'
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

        System.out.println("✅ Matching profiles created for user and user2.");
    }

    private Set<IdealTypeKeyword> createKeywords(String... keywords) {
        return Arrays.stream(keywords)
                .map(keyword -> idealTypeKeywordRepository.findByKeyword(keyword)
                        .orElseGet(() -> idealTypeKeywordRepository.save(IdealTypeKeyword.builder().keyword(keyword).build())))
                .collect(Collectors.toSet());
    }
}
