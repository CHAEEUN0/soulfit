package soulfit.soulfit.matching.swipe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.swipe.domain.Match;
import soulfit.soulfit.matching.swipe.domain.Swipe;
import soulfit.soulfit.matching.swipe.domain.SwipeType;
import soulfit.soulfit.matching.swipe.dto.MatchResponse;
import soulfit.soulfit.matching.swipe.dto.SwipeRequest;
import soulfit.soulfit.matching.swipe.dto.SwipeUserResponse;
import soulfit.soulfit.matching.swipe.repository.MatchRepository;
import soulfit.soulfit.matching.swipe.repository.SwipeRepository;
import soulfit.soulfit.profile.domain.UserProfile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SwipeServiceTest {

    @Autowired
    private SwipeService swipeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    private UserAuth userA, userB, userC;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        userA = new UserAuth("userA", "password", "userA@test.com");
        userB = new UserAuth("userB", "password", "userB@test.com");
        userC = new UserAuth("userC", "password", "userC@test.com");

        // 프로필 사진 URL을 포함한 UserProfile 생성 및 연결
        UserProfile profileA = new UserProfile();
        profileA.setProfileImageUrl("url_A");
        userA.setUserProfile(profileA);

        UserProfile profileB = new UserProfile();
        profileB.setProfileImageUrl("url_B");
        userB.setUserProfile(profileB);

        UserProfile profileC = new UserProfile();
        profileC.setProfileImageUrl("url_C");
        userC.setUserProfile(profileC);

        userRepository.saveAll(List.of(userA, userB, userC));
    }

    @Test
    @DisplayName("A가 B를 LIKE했을 때, 상호 좋아요가 아니면 매치가 생성되지 않는다.")
    void performSwipe_NoMutualLike_ShouldNotCreateMatch() {
        // given
        SwipeRequest swipeRequest = new SwipeRequest(userB.getId(), SwipeType.LIKE);

        // when
        MatchResponse response = swipeService.performSwipe(userA, swipeRequest);

        // then
        assertThat(response.isMatch()).isFalse();
        assertThat(response.getCreatedChatRoomId()).isNull();
        assertThat(swipeRepository.count()).isEqualTo(1);
        assertThat(matchRepository.count()).isZero();
    }

    @Test
    @DisplayName("B가 A를 LIKE한 상태에서 A가 B를 LIKE하면 매치가 생성된다.")
    void performSwipe_MutualLike_ShouldCreateMatch() {
        // given
        // B가 A를 미리 LIKE한 상황을 만든다.
        swipeRepository.save(Swipe.builder().swiper(userB).swiped(userA).type(SwipeType.LIKE).build());
        SwipeRequest swipeRequest = new SwipeRequest(userB.getId(), SwipeType.LIKE);

        // when
        MatchResponse response = swipeService.performSwipe(userA, swipeRequest);

        // then
        assertThat(response.isMatch()).isTrue();
        assertThat(swipeRepository.count()).isEqualTo(2);
        assertThat(matchRepository.count()).isEqualTo(1);

        Match match = matchRepository.findAll().get(0);
        assertThat(match.getUser1().getId()).isEqualTo(userA.getId());
        assertThat(match.getUser2().getId()).isEqualTo(userB.getId());
    }

    @Test
    @DisplayName("내가 LIKE한 사용자 목록을 정확히 반환한다.")
    void getMyLikedUsers_ShouldReturnCorrectList() {
        // given
        swipeService.performSwipe(userA, new SwipeRequest(userB.getId(), SwipeType.LIKE));
        swipeService.performSwipe(userA, new SwipeRequest(userC.getId(), SwipeType.LIKE));

        // when
        List<SwipeUserResponse> myLikedUsers = swipeService.getMyLikedUsers(userA);

        // then
        assertThat(myLikedUsers).hasSize(2);
        assertThat(myLikedUsers).extracting("username").containsExactlyInAnyOrder("userB", "userC");
        assertThat(myLikedUsers).extracting("profileImageUrl").containsExactlyInAnyOrder("url_B", "url_C");
    }

    @Test
    @DisplayName("나를 LIKE한 사용자 목록을 정확히 반환한다.")
    void getUsersWhoLikedMe_ShouldReturnCorrectList() {
        // given
        swipeService.performSwipe(userB, new SwipeRequest(userA.getId(), SwipeType.LIKE));
        swipeService.performSwipe(userC, new SwipeRequest(userA.getId(), SwipeType.LIKE));

        // when
        List<SwipeUserResponse> usersWhoLikedMe = swipeService.getUsersWhoLikedMe(userA);

        // then
        assertThat(usersWhoLikedMe).hasSize(2);
        assertThat(usersWhoLikedMe).extracting("username").containsExactlyInAnyOrder("userB", "userC");
    }
}
