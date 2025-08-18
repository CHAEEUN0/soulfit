package soulfit.soulfit.matching.swipe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.profile.domain.DrinkingHabit;
import soulfit.soulfit.matching.profile.domain.MatchingProfile;
import soulfit.soulfit.matching.profile.domain.SmokingHabit;
import soulfit.soulfit.matching.profile.repository.MatchingProfileRepository;
import soulfit.soulfit.matching.swipe.domain.Match;
import soulfit.soulfit.matching.swipe.domain.Swipe;
import soulfit.soulfit.matching.swipe.domain.SwipeType;
import soulfit.soulfit.matching.swipe.dto.MatchResponse;
import soulfit.soulfit.matching.swipe.dto.SwipeRequest;
import soulfit.soulfit.matching.swipe.dto.SwipeTargetUserResponse;
import soulfit.soulfit.matching.swipe.dto.SwipeUserResponse;
import soulfit.soulfit.matching.swipe.repository.MatchRepository;
import soulfit.soulfit.matching.swipe.repository.SwipeRepository;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.repository.UserProfileRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SwipeServiceTest {

    @Autowired
    private SwipeService swipeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private MatchingProfileRepository matchingProfileRepository;

    private UserAuth userA, userB, userC, userD, userE;
    private UserProfile profileA, profileB, profileC, profileD, profileE;
    private MatchingProfile matchingProfileB, matchingProfileC, matchingProfileD, matchingProfileE;

    @BeforeEach
    void setUp() {
        // Clear repositories before each test to ensure clean state
        matchRepository.deleteAll();
        swipeRepository.deleteAll();
        userProfileRepository.deleteAll();
        matchingProfileRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        userA = new UserAuth("userA", "password", "userA@test.com");
        userB = new UserAuth("userB", "password", "userB@test.com");
        userC = new UserAuth("userC", "password", "userC@test.com");
        userD = new UserAuth("userD", "password", "userD@test.com"); // For additional test cases
        userE = new UserAuth("userE", "password", "userE@test.com"); // For missing profile test

        userRepository.saveAll(List.of(userA, userB, userC, userD, userE));

        // UserProfile 생성 및 연결
        profileA = new UserProfile();
        profileA.setUserAuth(userA);
        profileA.setProfileImageUrl("url_A");
        profileA.setBirthDate(LocalDate.of(1990, 1, 1)); // Age 34
        profileA.setLatitude(37.5665);
        profileA.setLongitude(126.9780);
        profileA.setRegion("Seoul");
        userA.setUserProfile(profileA);

        profileB = new UserProfile();
        profileB.setUserAuth(userB);
        profileB.setProfileImageUrl("url_B");
        profileB.setBirthDate(LocalDate.of(1995, 5, 10)); // Age 29
        profileB.setLatitude(37.5700);
        profileB.setLongitude(126.9800);
        profileB.setRegion("Busan");
        userB.setUserProfile(profileB);

        profileC = new UserProfile();
        profileC.setUserAuth(userC);
        profileC.setProfileImageUrl("url_C");
        profileC.setBirthDate(LocalDate.of(1985, 10, 20)); // Age 39
        profileC.setLatitude(37.5600);
        profileC.setLongitude(126.9700);
        profileC.setRegion("Seoul");
        userC.setUserProfile(profileC);

        profileD = new UserProfile();
        profileD.setUserAuth(userD);
        profileD.setProfileImageUrl("url_D");
        profileD.setBirthDate(LocalDate.of(2000, 3, 15)); // Age 24
        profileD.setLatitude(37.5650);
        profileD.setLongitude(126.9750);
        profileD.setRegion("Incheon");
        userD.setUserProfile(profileD);

        // userE will have no UserProfile or MatchingProfile for testing missing data

        userProfileRepository.saveAll(List.of(profileA, profileB, profileC, profileD));

        // MatchingProfile 생성 및 연결
        matchingProfileB = MatchingProfile.builder()
                .userAuth(userB)
                .heightCm(175)
                .smoking(SmokingHabit.NON_SMOKER)
                .drinking(DrinkingHabit.SOMETIMES)
                .build();

        matchingProfileC = MatchingProfile.builder()
                .userAuth(userC)
                .heightCm(160)
                .smoking(SmokingHabit.OCCASIONAL)
                .drinking(DrinkingHabit.NEVER)
                .build();

        matchingProfileD = MatchingProfile.builder()
                .userAuth(userD)
                .heightCm(180)
                .smoking(SmokingHabit.REGULAR)
                .drinking(DrinkingHabit.DAILY)
                .build();

        matchingProfileRepository.saveAll(List.of(matchingProfileB, matchingProfileC, matchingProfileD));
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

    // --- New test cases for getPotentialSwipeTargets ---

    @Test
    @DisplayName("필터 없이 잠재적 스와이프 대상 목록을 반환한다.")
    void getPotentialSwipeTargets_noFilters_returnsAllEligibleUsers() {
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, null, null, null, null, null, null, null
        );

        assertThat(targets).hasSize(3);
        assertThat(targets).extracting("userId").containsExactlyInAnyOrder(userB.getId(), userC.getId(), userD.getId());
    }

    @Test
    @DisplayName("이미 스와이프한 사용자를 잠재적 스와이프 대상에서 제외한다.")
    void getPotentialSwipeTargets_filtersOutAlreadySwipedUsers() {
        swipeService.performSwipe(userA, new SwipeRequest(userB.getId(), SwipeType.LIKE));

        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, null, null, null, null, null, null, null
        );

        assertThat(targets).hasSize(2);
        assertThat(targets).extracting("userId").doesNotContain(userB.getId());
        assertThat(targets).extracting("userId").containsExactlyInAnyOrder(userC.getId(), userD.getId());
    }

    @Test
    @DisplayName("나이로 잠재적 스와이프 대상을 필터링한다.")
    void getPotentialSwipeTargets_filtersByAge() {
        // userA (34), userB (29), userC (39), userD (24)
        // Filter for users between 25 and 35
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, null, null, 25, 35, null, null, null
        );

        assertThat(targets).hasSize(2);
        assertThat(targets).extracting("userId").containsExactlyInAnyOrder(userB.getId(), userD.getId());
    }

    @Test
    @DisplayName("키로 잠재적 스와이프 대상을 필터링한다.")
    void getPotentialSwipeTargets_filtersByHeight() {
        // userB (175), userC (160), userD (180)
        // Filter for users between 170 and 179 cm
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, 170, 179, null, null, null, null, null
        );

        assertThat(targets).hasSize(1);
        assertThat(targets).extracting("userId").containsExactly(userB.getId());
    }

    @Test
    @DisplayName("지역으로 잠재적 스와이프 대상을 필터링한다.")
    void getPotentialSwipeTargets_filtersByRegion() {
        // userB (Busan), userC (Seoul), userD (Incheon)
        // Filter for Seoul
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                "Seoul", null, null, null, null, null, null, null
        );

        assertThat(targets).hasSize(1);
        assertThat(targets).extracting("userId").containsExactly(userC.getId());
    }

    @Test
    @DisplayName("흡연 습관으로 잠재적 스와이프 대상을 필터링한다.")
    void getPotentialSwipeTargets_filtersBySmokingHabit() {
        // userB (NON_SMOKER), userC (OCCASIONAL), userD (REGULAR)
        // Filter for NON_SMOKER
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, null, null, null, null, null, SmokingHabit.NON_SMOKER.name(), null
        );

        assertThat(targets).hasSize(1);
        assertThat(targets).extracting("userId").containsExactly(userB.getId());
    }

    @Test
    @DisplayName("음주 습관으로 잠재적 스와이프 대상을 필터링한다.")
    void getPotentialSwipeTargets_filtersByDrinkingHabit() {
        // userB (SOMETIMES), userC (NEVER), userD (DAILY)
        // Filter for NEVER
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, null, null, null, null, null, null, DrinkingHabit.NEVER.name()
        );

        assertThat(targets).hasSize(1);
        assertThat(targets).extracting("userId").containsExactly(userC.getId());
    }

    @Test
    @DisplayName("거리로 잠재적 스와이프 대상을 필터링한다.")
    void getPotentialSwipeTargets_filtersByDistance() {
        // userA (37.5665, 126.9780)
        // userB (37.5700, 126.9800) - ~0.4km
        // userC (37.5600, 126.9700) - ~1.0km
        // userD (37.5650, 126.9750) - ~0.3km

        // Filter for max distance 0.5 km
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, null, null, null, null, 0.5, null, null
        );

        assertThat(targets).hasSize(2);
        assertThat(targets).extracting("userId").containsExactlyInAnyOrder(userB.getId(), userD.getId());
    }

    @Test
    @DisplayName("프로필 데이터가 누락된 사용자를 잠재적 스와이프 대상에서 제외한다.")
    void getPotentialSwipeTargets_handlesMissingProfileData() {
        // userE has no UserProfile or MatchingProfile
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                userA, profileA.getLatitude(), profileA.getLongitude(),
                null, null, null, null, null, null, null, null
        );

        assertThat(targets).hasSize(3);
        assertThat(targets).extracting("userId").doesNotContain(userE.getId());
        assertThat(targets).extracting("userId").containsExactlyInAnyOrder(userB.getId(), userC.getId(), userD.getId());
    }
}