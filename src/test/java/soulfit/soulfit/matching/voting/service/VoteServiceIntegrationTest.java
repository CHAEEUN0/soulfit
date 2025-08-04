package soulfit.soulfit.matching.voting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.voting.domain.VoteForm;
import soulfit.soulfit.matching.voting.dto.VoteFormCreateRequest;
import soulfit.soulfit.matching.voting.dto.VoteFormResponse;
import soulfit.soulfit.matching.voting.dto.VoteRequest;
import soulfit.soulfit.matching.voting.dto.VoteResultResponse;
import soulfit.soulfit.matching.voting.repository.VoteFormRepository;
import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.repository.UserProfileRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class VoteServiceIntegrationTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoteFormRepository voteFormRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private UserAuth userA; // 투표 생성자
    private UserAuth userB; // 투표자

    Logger logger = LoggerFactory.getLogger(VoteServiceIntegrationTest.class);

    @BeforeEach
    void setUp() {
        userA = userRepository.save(new UserAuth("userA", "password", "ROLE_USER"));
        userB = userRepository.save(new UserAuth("userB", "password", "ROLE_USER"));

        // userA의 프로필 생성 및 이미지 URL 설정
        UserProfile userAProfile = new UserProfile(userA, LocalDate.of(1990, 1, 1), Gender.FEMALE);
        userAProfile.setProfileImageUrl("http://example.com/userA_profile.jpg");
        userProfileRepository.save(userAProfile);

        // userB의 프로필 생성 (이미지 URL은 설정하지 않음)
        UserProfile userBProfile = new UserProfile(userB, LocalDate.of(1992, 5, 10), Gender.MALE);
        userProfileRepository.save(userBProfile);
    }

    @Test
    @DisplayName("사용자가 새로운 투표를 성공적으로 생성한다")
    void createVoteForm() {
        // given
        VoteFormCreateRequest request = new VoteFormCreateRequest(
                "가장 마음에 드는 프로필 사진은?",
                "A/B 투표입니다.",
                Arrays.asList("사진 1", "사진 2")
        );

        // when
        Long voteFormId = voteService.createVoteForm(userA, request);

        // then
        assertThat(voteFormId).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자가 생성된 투표에 투표한다")
    void vote() {
        // given
        VoteForm voteForm = createTestVoteForm();
        Long voteOptionId = voteForm.getOptions().get(0).getId(); // 첫 번째 옵션의 실제 ID 사용
        VoteRequest request = new VoteRequest(voteForm.getId(), voteOptionId);

        // when
        voteService.vote(userB.getId(), request);

        // then
        // (성공 시 예외가 발생하지 않음)
    }

    @Test
    @DisplayName("중복 투표 시 예외가 발생한다")
    void vote_duplicate_throwException() {
        // given
        VoteForm voteForm = createTestVoteForm();
        Long voteOptionId = voteForm.getOptions().get(0).getId(); // 첫 번째 옵션의 실제 ID 사용
        VoteRequest request = new VoteRequest(voteForm.getId(), voteOptionId);
        voteService.vote(userB.getId(), request); // 첫 번째 투표

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            voteService.vote(userB.getId(), request); // 두 번째 투표
        });
    }

    @Test
    @DisplayName("투표 생성자가 결과를 정확히 조회한다")
    void getVoteResults_byCreator() {
        // given
        VoteForm voteForm = createTestVoteForm();
        Long voteOptionId = voteForm.getOptions().get(0).getId(); // 첫 번째 옵션의 실제 ID 사용
        // userB가 1번 옵션에 투표
        voteService.vote(userB.getId(), new VoteRequest(voteForm.getId(), voteOptionId));

        // when
        VoteResultResponse results = voteService.getVoteResults(userA, voteForm.getId());

        List<VoteResultResponse.OptionResult> results1 = results.getResults();

        for (VoteResultResponse.OptionResult optionResult : results1) {
            logger.info(optionResult.getOptionId()+"");
            logger.info(optionResult.getLabel() + "");
            logger.info(optionResult.getVoteCount() + "");
        }

        // then
        assertThat(results.getVoteFormId()).isEqualTo(voteForm.getId());
        assertThat(results.getResults()).hasSize(2);
        assertThat(results.getResults().get(0).getVoteCount()).isEqualTo(1);
        assertThat(results.getResults().get(1).getVoteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("투표 생성자가 아닌 사용자가 결과 조회 시 예외가 발생한다")
    void getVoteResults_byOtherUser_throwException() {
        // given
        VoteForm voteForm = createTestVoteForm();

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            voteService.getVoteResults(userB, voteForm.getId()); // userB가 결과 조회 시도
        });
    }

    private VoteForm createTestVoteForm() {
        VoteFormCreateRequest request = new VoteFormCreateRequest(
                "테스트 투표",
                "설명",
                Arrays.asList("옵션1", "옵션2")
        );
        Long voteFormId = voteService.createVoteForm(userA, request);
        return voteFormRepository.findById(voteFormId).orElseThrow();
    }

    @Test
    @DisplayName("투표 폼 조회 시 투표 생성자의 프로필 이미지 URL이 포함된다")
    void getVoteForm_includesCreatorProfileImageUrl() {
        // given
        // userA가 생성한 투표 폼 (setUp에서 프로필 이미지 URL 설정됨)
        VoteForm voteForm = createTestVoteForm();

        // when
        VoteFormResponse response = voteService.getVoteForm(voteForm.getId());

        // then
        assertThat(response.getId()).isEqualTo(voteForm.getId());
        assertThat(response.getCreatorId()).isEqualTo(userA.getId());
        assertThat(response.getCreatorUsername()).isEqualTo(userA.getUsername());
        assertThat(response.getCreatorProfileImageUrl()).isEqualTo("http://example.com/userA_profile.jpg");
    }
}
