
package soulfit.soulfit.matching.review.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.chat.ChatParticipant;
import soulfit.soulfit.chat.ChatParticipantRepository;
import soulfit.soulfit.chat.ChatRoomType;
import soulfit.soulfit.chat.ChatRoom;
import soulfit.soulfit.chat.ChatRoomRepository;
import soulfit.soulfit.matching.review.domain.Review;
import soulfit.soulfit.matching.review.domain.ReviewKeyword;
import soulfit.soulfit.matching.review.dto.ReviewRequestDto;
import soulfit.soulfit.matching.review.dto.ReviewResponseDto;
import soulfit.soulfit.matching.review.repository.ReviewKeywordRepository;
import soulfit.soulfit.matching.review.repository.ReviewRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewKeywordRepository reviewKeywordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UserAuth reviewer;
    private UserAuth reviewee;
    private ChatRoom chatRoom;
    private ChatParticipant reviewerParticipant;
    private ChatParticipant revieweeParticipant;
    private ReviewRequestDto requestDto;

    @BeforeEach
    void setUp() {
        reviewer = new UserAuth("reviewer", "password123", "reviewer@test.com");
        reviewer.setId(1L);

        reviewee = new UserAuth("reviewee", "password123", "reviewee@test.com");
        reviewee.setId(2L);

        chatRoom = ChatRoom.builder()
                .id(1L)
                .name("Test ChatRoom")
                .type(ChatRoomType.Direct)
                .build();

        reviewerParticipant = ChatParticipant.builder().chatRoom(chatRoom).user(reviewer).build();
        revieweeParticipant = ChatParticipant.builder().chatRoom(chatRoom).user(reviewee).build();

        requestDto = new TestReviewRequestDto(2L, 1L, "Great conversation!", List.of("Polite", "Funny"));
    }

    // Helper DTO class for testing
    private static class TestReviewRequestDto extends ReviewRequestDto {
        public TestReviewRequestDto(Long revieweeId, Long chatRoomId, String comment, List<String> keywords) {
            super();
            // Using reflection to set private fields, or make them protected for testing
            try {
                var field = ReviewRequestDto.class.getDeclaredField("revieweeId");
                field.setAccessible(true);
                field.set(this, revieweeId);

                field = ReviewRequestDto.class.getDeclaredField("chatRoomId");
                field.setAccessible(true);
                field.set(this, chatRoomId);

                field = ReviewRequestDto.class.getDeclaredField("comment");
                field.setAccessible(true);
                field.set(this, comment);

                field = ReviewRequestDto.class.getDeclaredField("keywords");
                field.setAccessible(true);
                field.set(this, keywords);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_Success() {
        // given
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewer)).thenReturn(true);
        when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewee)).thenReturn(true);
        when(reviewRepository.existsByChatRoomAndReviewer(chatRoom, reviewer)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewee));
        Set<ReviewKeyword> keywords = Set.of(new ReviewKeyword(1L, "Polite"), new ReviewKeyword(2L, "Funny"));
        when(reviewKeywordRepository.findByKeywordIn(List.of("Polite", "Funny"))).thenReturn(keywords);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReviewResponseDto response = reviewService.createReview(reviewer, requestDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getComment()).isEqualTo("Great conversation!");
        assertThat(response.getKeywords()).containsExactlyInAnyOrder("Polite", "Funny");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 자기 자신 리뷰")
    void createReview_Fail_ReviewingSelf() {
        // given
        requestDto = new TestReviewRequestDto(1L, 1L, "Self review", List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(reviewer, requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("자기 자신을 리뷰할 수 없습니다.");
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 채팅방을 찾을 수 없음")
    void createReview_Fail_ChatRoomNotFound() {
        // given
        when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            reviewService.createReview(reviewer, requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("채팅방을 찾을 수 없습니다.");
    }



    @Test
    @DisplayName("리뷰 생성 실패 - 중복 리뷰")
    void createReview_Fail_DuplicateReview() {
        // given
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewer)).thenReturn(true);
        when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewee)).thenReturn(true);
        when(userRepository.findById(requestDto.getRevieweeId())).thenReturn(Optional.of(reviewee));
        when(reviewRepository.existsByChatRoomAndReviewer(chatRoom, reviewer)).thenReturn(true);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(reviewer, requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("이미 해당 채팅방에 대한 리뷰를 작성했습니다.");
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 리뷰어 또는 리뷰 대상자가 채팅방 참가자가 아님")
    void createReview_Fail_NotChatRoomParticipant() {
        // given
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewer)).thenReturn(true);
        when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewee)).thenReturn(false); // reviewee is not a participant
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewee));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(reviewer, requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("리뷰어 또는 리뷰 대상자가 해당 채팅방의 참가자가 아닙니다.");
    }

    @Test
    @DisplayName("상위 리뷰 키워드 조회 성공")
    void getTopKeywordsForUser_Success() {
        // given
        Long userId = 2L;
        int limit = 3;
        List<String> topKeywords = List.of("Funny", "Polite", "Kind");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(reviewRepository.findTopKeywordsByRevieweeId(anyLong(), any(Pageable.class)))
                .thenReturn(topKeywords);

        // when
        List<String> result = reviewService.getTopKeywordsForUser(userId, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).containsExactly("Funny", "Polite", "Kind");
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findTopKeywordsByRevieweeId(userId, PageRequest.of(0, limit));
    }

    @Test
    @DisplayName("상위 리뷰 키워드 조회 실패 - 사용자를 찾을 수 없음")
    void getTopKeywordsForUser_Fail_UserNotFound() {
        // given
        Long nonExistentUserId = 99L;
        int limit = 3;
        when(userRepository.existsById(nonExistentUserId)).thenReturn(false);

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            reviewService.getTopKeywordsForUser(nonExistentUserId, limit);
        });
        assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }
}
