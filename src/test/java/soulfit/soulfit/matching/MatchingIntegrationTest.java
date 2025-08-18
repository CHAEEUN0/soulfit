package soulfit.soulfit.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.common.ImageUploadService;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;
import soulfit.soulfit.matching.conversation.dto.ConversationRequestDto;
import soulfit.soulfit.matching.conversation.dto.ConversationResponseDto;
import soulfit.soulfit.matching.conversation.dto.UpdateRequestStatusDto;
import soulfit.soulfit.matching.conversation.repository.ConversationRequestRepository;
import soulfit.soulfit.matching.conversation.service.ConversationService;
import soulfit.soulfit.matching.profile.domain.DrinkingHabit;
import soulfit.soulfit.matching.profile.domain.MatchingProfile;
import soulfit.soulfit.matching.profile.domain.Religion;
import soulfit.soulfit.matching.profile.domain.SmokingHabit;
import soulfit.soulfit.matching.profile.domain.Visibility;
import soulfit.soulfit.matching.profile.dto.MatchingProfileRequestDto;
import soulfit.soulfit.matching.profile.dto.MatchingProfileResponseDto;
import soulfit.soulfit.matching.profile.repository.IdealTypeKeywordRepository;
import soulfit.soulfit.matching.profile.repository.MatchingProfileRepository;
import soulfit.soulfit.matching.profile.service.MatchingProfileService;
import soulfit.soulfit.matching.swipe.domain.Match;
import soulfit.soulfit.matching.swipe.domain.SwipeType;
import soulfit.soulfit.matching.swipe.dto.MatchResponse;
import soulfit.soulfit.matching.swipe.dto.SwipeRequest;
import soulfit.soulfit.matching.swipe.repository.MatchRepository;
import soulfit.soulfit.matching.swipe.repository.SwipeRepository;
import soulfit.soulfit.matching.swipe.service.SwipeService;
import soulfit.soulfit.matching.voting.domain.VoteForm;
import soulfit.soulfit.matching.voting.dto.VoteFormCreateRequest;
import soulfit.soulfit.matching.voting.dto.VoteFormResponse;
import soulfit.soulfit.matching.voting.dto.VoteRequest;
import soulfit.soulfit.matching.voting.dto.VoteResultResponse;
import soulfit.soulfit.matching.voting.repository.VoteFormRepository;
import soulfit.soulfit.matching.voting.service.VoteService;
import soulfit.soulfit.notification.service.NotificationService;
import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.repository.UserProfileRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MatchingIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private MatchingProfileRepository matchingProfileRepository;
    @Autowired
    private IdealTypeKeywordRepository idealTypeKeywordRepository;
    @Autowired
    private SwipeRepository swipeRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private ConversationRequestRepository conversationRequestRepository;
    @Autowired
    private VoteFormRepository voteFormRepository;

    @Autowired
    private MatchingProfileService matchingProfileService;
    @Autowired
    private SwipeService swipeService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private VoteService voteService;

    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private ImageUploadService imageUploadService;

    private UserAuth userA;
    private UserAuth userB;
    private UserAuth userC; // For voting scenario

    @BeforeEach
    void setUp() throws IOException {
        // Clear repositories to ensure a clean state for each test
        matchRepository.deleteAll();
        swipeRepository.deleteAll();
        conversationRequestRepository.deleteAll();
        voteFormRepository.deleteAll();
        idealTypeKeywordRepository.deleteAll();
        matchingProfileRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Setup UserAuth
        userA = userRepository.save(new UserAuth("userA", "password", "userA@example.com"));
        userB = userRepository.save(new UserAuth("userB", "password", "userB@example.com"));
        userC = userRepository.save(new UserAuth("userC", "password", "userC@example.com"));

        // Setup UserProfile for userA and userB
        UserProfile profileA = new UserProfile(userA, LocalDate.of(1990, 1, 1), Gender.FEMALE);
        profileA.setProfileImageUrl("http://example.com/profileA.jpg");
        userA.setUserProfile(profileA);
        userProfileRepository.save(profileA);

        UserProfile profileB = new UserProfile(userB, LocalDate.of(1992, 5, 5), Gender.MALE);
        profileB.setProfileImageUrl("http://example.com/profileB.jpg");
        userB.setUserProfile(profileB);
        userProfileRepository.save(profileB);

        // Setup initial MatchingProfile for userA and userB (will be updated in tests)
        matchingProfileRepository.save(MatchingProfile.builder().userAuth(userA).build());
        matchingProfileRepository.save(MatchingProfile.builder().userAuth(userB).build());

        // Mock ImageUploadService for voting scenario
        when(imageUploadService.uploadImage(any(), any())).thenReturn("http://example.com/uploaded_image.jpg");
    }

    @Test
    @DisplayName("시나리오 1: 프로필 업데이트 -> 스와이프 -> 상호 좋아요 -> 매치 생성 -> 대화 요청 및 수락")
    void scenario1_profileUpdate_swipe_mutualLike_match_conversation() {
        // 1. profile - UserA 매칭 프로필 업데이트
        Set<String> userAIdealTypes = new HashSet<>(Arrays.asList("운동", "여행"));
        MatchingProfileRequestDto userAProfileDto = new MatchingProfileRequestDto(
                "UserA bio", "Engineer", 165, 55, Religion.NONE,
                SmokingHabit.NON_SMOKER, DrinkingHabit.SOMETIMES, Visibility.PUBLIC, userAIdealTypes
        );
        MatchingProfileResponseDto updatedAProfile = matchingProfileService.updateMyProfile(userA, userAProfileDto);
        assertThat(updatedAProfile.bio()).isEqualTo("UserA bio");
        assertThat(updatedAProfile.idealTypes()).containsExactlyInAnyOrder("운동", "여행");

        // 2. profile - UserB 매칭 프로필 업데이트
        Set<String> userBIdealTypes = new HashSet<>(Arrays.asList("독서", "음악"));
        MatchingProfileRequestDto userBProfileDto = new MatchingProfileRequestDto(
                "UserB bio", "Designer", 180, 70, Religion.CHRISTIANITY,
                SmokingHabit.OCCASIONAL, DrinkingHabit.DAILY, Visibility.PUBLIC, userBIdealTypes
        );
        MatchingProfileResponseDto updatedBProfile = matchingProfileService.updateMyProfile(userB, userBProfileDto);
        assertThat(updatedBProfile.job()).isEqualTo("Designer");
        assertThat(updatedBProfile.idealTypes()).containsExactlyInAnyOrder("독서", "음악");

        // 3. swipe - UserA가 UserB를 LIKE
        SwipeRequest swipeAtoB = new SwipeRequest(userB.getId(), SwipeType.LIKE);
        MatchResponse matchResponse1 = swipeService.performSwipe(userA, swipeAtoB);
        assertThat(matchResponse1.isMatch()).isFalse();
        assertThat(swipeRepository.findBySwiperAndSwipedAndType(userA, userB, SwipeType.LIKE)).isPresent();
        assertThat(matchRepository.count()).isZero();

        // 4. swipe - UserB가 UserA를 LIKE
        SwipeRequest swipeBtoA = new SwipeRequest(userA.getId(), SwipeType.LIKE);
        MatchResponse matchResponse2 = swipeService.performSwipe(userB, swipeBtoA);
        assertThat(matchResponse2.isMatch()).isTrue();
        assertThat(swipeRepository.findBySwiperAndSwipedAndType(userB, userA, SwipeType.LIKE)).isPresent();
        assertThat(matchRepository.count()).isEqualTo(1);
        Match createdMatch = matchRepository.findAll().get(0);
        assertThat(Set.of(createdMatch.getUser1().getId(), createdMatch.getUser2().getId()))
                .containsExactlyInAnyOrder(userA.getId(), userB.getId());

        // 5. conversation - UserA가 UserB에게 대화 신청
        ConversationRequestDto convRequestDto = new ConversationRequestDto(userB.getId(), "안녕하세요, 매치되어서 기뻐요!");
        ConversationResponseDto createdConvRequest = conversationService.createConversationRequest(userA, convRequestDto);
        assertThat(createdConvRequest.status()).isEqualTo(RequestStatus.PENDING);
        verify(notificationService, times(1)).sendNotification(any(), any(), any(), any(), any(), anyLong());

        // 6. conversation - UserB가 대화 신청 수락
        UpdateRequestStatusDto updateStatusDto = new UpdateRequestStatusDto("ACCEPTED");
        ConversationResponseDto acceptedConvRequest = conversationService.updateRequestStatus(createdConvRequest.id(), userB, updateStatusDto);
        assertThat(acceptedConvRequest.status()).isEqualTo(RequestStatus.ACCEPTED);
        verify(notificationService, times(2)).sendNotification(any(), any(), any(), any(), any(), anyLong()); // 1 for initial, 1 for accept

        // 7. conversation - UserA가 보낸 대화 신청 목록 조회
        List<ConversationResponseDto> sentRequests = conversationService.getSentRequests(userA, RequestStatus.ACCEPTED);
        assertThat(sentRequests).hasSize(1);
        assertThat(sentRequests.get(0).id()).isEqualTo(acceptedConvRequest.id());
        assertThat(sentRequests.get(0).status()).isEqualTo(RequestStatus.ACCEPTED);

        // 8. conversation - UserB가 받은 대화 신청 목록 조회
        List<ConversationResponseDto> receivedRequests = conversationService.getReceivedRequests(userB, RequestStatus.ACCEPTED);
        assertThat(receivedRequests).hasSize(1);
        assertThat(receivedRequests.get(0).id()).isEqualTo(acceptedConvRequest.id());
        assertThat(receivedRequests.get(0).status()).isEqualTo(RequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("시나리오 2: 프로필 업데이트 -> 스와이프 -> 매치 없음 -> 대화 요청 (거부)")
    void scenario2_profileUpdate_swipe_noMatch_conversationRejected() {
        // 1. profile - UserA 매칭 프로필 업데이트 (간략화)
        matchingProfileService.updateMyProfile(userA, new MatchingProfileRequestDto(
                "UserA bio", "Engineer", 165, 55, Religion.NONE,
                SmokingHabit.NON_SMOKER, DrinkingHabit.SOMETIMES, Visibility.PUBLIC, new HashSet<>()
        ));

        // 2. profile - UserB 매칭 프로필 업데이트 (간략화)
        matchingProfileService.updateMyProfile(userB, new MatchingProfileRequestDto(
                "UserB bio", "Designer", 180, 70, Religion.CHRISTIANITY,
                SmokingHabit.OCCASIONAL, DrinkingHabit.DAILY, Visibility.PUBLIC, new HashSet<>()
        ));

        // 3. swipe - UserA가 UserB를 LIKE
        swipeService.performSwipe(userA, new SwipeRequest(userB.getId(), SwipeType.LIKE));

        // 4. swipe - UserB가 UserA를 DISLIKE
        SwipeRequest swipeBtoA_dislike = new SwipeRequest(userA.getId(), SwipeType.DISLIKE);
        MatchResponse matchResponse = swipeService.performSwipe(userB, swipeBtoA_dislike);
        assertThat(matchResponse.isMatch()).isFalse();
        assertThat(matchRepository.count()).isZero(); // 매치 생성 안됨

        // 5. conversation - UserA가 UserB에게 대화 신청 (매치 없어도 가능)
        ConversationRequestDto convRequestDto = new ConversationRequestDto(userB.getId(), "안녕하세요!");
        ConversationResponseDto createdConvRequest = conversationService.createConversationRequest(userA, convRequestDto);
        assertThat(createdConvRequest.status()).isEqualTo(RequestStatus.PENDING);
        verify(notificationService, times(1)).sendNotification(any(), any(), any(), any(), any(), anyLong());

        // 6. conversation - UserB가 대화 신청 거절
        UpdateRequestStatusDto updateStatusDto = new UpdateRequestStatusDto("REJECTED");
        ConversationResponseDto rejectedConvRequest = conversationService.updateRequestStatus(createdConvRequest.id(), userB, updateStatusDto);
        assertThat(rejectedConvRequest.status()).isEqualTo(RequestStatus.REJECTED);
        // NotificationService 호출 검증 (거절 알림이 있다면 추가)
    }

    @Test
    @DisplayName("시나리오 3: 투표 양식 생성 및 투표 결과 확인")
    void scenario3_createVoteForm_vote_viewResults() throws IOException {
        // 1. voting - UserA가 투표 양식 생성 (이미지 포함)
        String testImageUrl = "http://example.com/uploaded_image.jpg";
        VoteFormCreateRequest createRequest = new VoteFormCreateRequest(
                "이 프로필 사진 어떤가요?",
                "사진에 대한 투표입니다.",
                Arrays.asList("좋아요", "별로예요"),
                "IMAGE",
                testImageUrl
        );
        Long voteFormId = voteService.createVoteForm(userA, createRequest);
        assertThat(voteFormId).isNotNull();
        
        

        // 2. voting - UserB가 투표 양식 조회 및 투표
        VoteFormResponse voteFormResponse = voteService.getVoteForm(voteFormId);
        assertThat(voteFormResponse.getTitle()).isEqualTo("이 프로필 사진 어떤가요?");
        assertThat(voteFormResponse.getImageUrl()).isEqualTo(testImageUrl);
        assertThat(voteFormResponse.getOptions()).hasSize(2);
        Long option1Id = voteFormResponse.getOptions().get(0).getId(); // "좋아요" 옵션 ID

        voteService.vote(userB.getId(), new VoteRequest(voteFormId, option1Id));

        // 3. voting - UserC가 투표 양식 조회 및 투표
        voteService.vote(userC.getId(), new VoteRequest(voteFormId, option1Id));

        // 4. voting - UserA가 투표 결과 조회
        VoteResultResponse results = voteService.getVoteResults(userA, voteFormId);
        assertThat(results.getVoteFormId()).isEqualTo(voteFormId);
        assertThat(results.getResults()).hasSize(2);
        assertThat(results.getResults().get(0).getLabel()).isEqualTo("좋아요");
        assertThat(results.getResults().get(0).getVoteCount()).isEqualTo(2);
        assertThat(results.getResults().get(1).getLabel()).isEqualTo("별로예요");
        assertThat(results.getResults().get(1).getVoteCount()).isEqualTo(0);

        // 5. voting - UserB가 투표 결과 조회 시도 (권한 없음)
        assertThatThrownBy(() -> voteService.getVoteResults(userB, voteFormId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("투표 결과를 조회할 권한이 없습니다.");
    }
}
