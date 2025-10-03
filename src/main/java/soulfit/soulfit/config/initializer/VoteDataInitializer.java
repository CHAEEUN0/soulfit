package soulfit.soulfit.config.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.voting.domain.UserVote;
import soulfit.soulfit.matching.voting.domain.VoteForm;
import soulfit.soulfit.matching.voting.domain.VoteOption;
import soulfit.soulfit.matching.voting.repository.UserVoteRepository;
import soulfit.soulfit.matching.voting.repository.VoteFormRepository;

import java.util.List;

@Component
@Profile("!test")
@Order(10)
@RequiredArgsConstructor
public class VoteDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VoteFormRepository voteFormRepository;
    private final UserVoteRepository userVoteRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if vote data already exists to prevent duplicates
        if (voteFormRepository.count() > 0) {
            return;
        }

        // Fetch users created by UserInitializer
        UserAuth user1 = userRepository.findByUsername("user").orElse(null);
        UserAuth user2 = userRepository.findByUsername("user2").orElse(null);

        if (user1 == null || user2 == null) {
            System.out.println("Sample users not found, skipping vote data initialization.");
            return;
        }

        // --- Create Sample Vote 1 ---
        VoteForm voteForm1 = VoteForm.builder()
                .creator(user1)
                .title("첫인상 투표")
                .description("저의 첫인상은 어떤가요?")
                .targetType(VoteForm.TargetType.PROFILE)
                .active(true)
                .multiSelect(false)
                .build();

        VoteOption option1_1 = VoteOption.builder().voteForm(voteForm1).label("차분해 보여요").sortOrder(1).build();
        VoteOption option1_2 = VoteOption.builder().voteForm(voteForm1).label("활발해 보여요").sortOrder(2).build();
        VoteOption option1_3 = VoteOption.builder().voteForm(voteForm1).label("엉뚱해 보여요").sortOrder(3).build();

        voteForm1.getOptions().addAll(List.of(option1_1, option1_2, option1_3));
        voteFormRepository.save(voteForm1);

        // user2 votes on voteForm1
        UserVote vote1 = UserVote.createVote(user2, user1, voteForm1, option1_1);
        userVoteRepository.save(vote1);

        System.out.println("Sample vote data created.");
    }
}
