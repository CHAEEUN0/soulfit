package soulfit.soulfit.config.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.swipe.domain.Match;
import soulfit.soulfit.matching.swipe.domain.Swipe;
import soulfit.soulfit.matching.swipe.domain.SwipeType;
import soulfit.soulfit.matching.swipe.repository.MatchRepository;
import soulfit.soulfit.matching.swipe.repository.SwipeRepository;

import java.util.Optional;

@Component
@Profile("!test")
@Order(5) // Runs after UserInitializer and MatchingProfileInitializer
@RequiredArgsConstructor
public class SwipeInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;

    @Override
    public void run(String... args) throws Exception {
        if (swipeRepository.count() > 0) {
            return; // Data already initialized
        }

        // Fetch users
        UserAuth user = userRepository.findByUsername("user").orElseThrow(() -> new RuntimeException("User 'user' not found."));
        UserAuth user2 = userRepository.findByUsername("user2").orElseThrow(() -> new RuntimeException("User 'user2' not found."));
        UserAuth user3 = userRepository.findByUsername("user3").orElseThrow(() -> new RuntimeException("User 'user3' not found."));
        UserAuth user4 = userRepository.findByUsername("user4").orElseThrow(() -> new RuntimeException("User 'user4' not found."));
        UserAuth user5 = userRepository.findByUsername("user5").orElseThrow(() -> new RuntimeException("User 'user5' not found."));
        UserAuth user6 = userRepository.findByUsername("user6").orElseThrow(() -> new RuntimeException("User 'user6' not found."));

        // Scenario 1: user likes user2
        createSwipe(user, user2, SwipeType.LIKE);

        // Scenario 2: user2 likes user (mutual like, should create a match)
        createSwipe(user2, user, SwipeType.LIKE);
        createMatch(user, user2); // Manually create match for initializer

        // Scenario 3: user dislikes user3
        createSwipe(user, user3, SwipeType.DISLIKE);

        // Scenario 4: user4 likes user
        createSwipe(user4, user, SwipeType.LIKE);

        // Scenario 5: user5 likes user6
        createSwipe(user5, user6, SwipeType.LIKE);

        System.out.println("✅ Sample swipe data created.");
    }

    private void createSwipe(UserAuth swiper, UserAuth swiped, SwipeType type) {
        Swipe swipe = Swipe.builder()
                .swiper(swiper)
                .swiped(swiped)
                .type(type)
                .build();
        swipeRepository.save(swipe);
    }

    private void createMatch(UserAuth user1, UserAuth user2) {
        // Check if match already exists to prevent duplicates
        Optional<Match> existingMatch = matchRepository.findByUser1AndUser2(user1, user2);
        if (existingMatch.isEmpty()) {
            existingMatch = matchRepository.findByUser2AndUser1(user1, user2);
        }

        if (existingMatch.isEmpty()) {
            Match match = Match.builder()
                    .user1(user1)
                    .user2(user2)
                    .chatRoomId(null) // Chat room ID can be null for initial sample data
                    .build();
            matchRepository.save(match);
        }
    }
}
