
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
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;
import soulfit.soulfit.matching.conversation.repository.ConversationRequestRepository;
import soulfit.soulfit.matching.review.domain.Review;
import soulfit.soulfit.matching.review.domain.ReviewKeyword;
import soulfit.soulfit.matching.review.dto.ReviewRequestDto;
import soulfit.soulfit.matching.review.dto.ReviewResponseDto;
import soulfit.soulfit.matching.review.repository.ReviewKeywordRepository;
import soulfit.soulfit.matching.review.repository.ReviewRepository;

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
    private ConversationRequestRepository conversationRequestRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UserAuth reviewer;
    private UserAuth reviewee;
    private ConversationRequest conversationRequest;
    private ReviewRequestDto requestDto;

    @BeforeEach
    void setUp() {
        reviewer = new UserAuth("reviewer", "password123", "reviewer@test.com");
        reviewer.setId(1L);

        reviewee = new UserAuth("reviewee", "password123", "reviewee@test.com");
        reviewee.setId(2L);

        conversationRequest = ConversationRequest.builder()
                .fromUser(reviewer)
                .toUser(reviewee)
                .message("test message")
                .build();
        try {
            java.lang.reflect.Field idField = ConversationRequest.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(conversationRequest, 1L);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        conversationRequest.updateStatus(RequestStatus.ACCEPTED);

        requestDto = new TestReviewRequestDto(2L, 1L, "Great conversation!", List.of("Polite", "Funny"));
    }

    // Helper DTO class for testing
    private static class TestReviewRequestDto extends ReviewRequestDto {
        public TestReviewRequestDto(Long revieweeId, Long conversationRequestId, String comment, List<String> keywords) {
            super();
            // Using reflection to set private fields, or make them protected for testing
            try {
                var field = ReviewRequestDto.class.getDeclaredField("revieweeId");
                field.setAccessible(true);
                field.set(this, revieweeId);

                field = ReviewRequestDto.class.getDeclaredField("conversationRequestId");
                field.setAccessible(true);
                field.set(this, conversationRequestId);

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
        when(conversationRequestRepository.findById(1L)).thenReturn(Optional.of(conversationRequest));
        when(reviewRepository.existsByConversationRequestAndReviewer(conversationRequest, reviewer)).thenReturn(false);
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
    @DisplayName("리뷰 생성 실패 - 대화가 존재하지 않음")
    void createReview_Fail_ConversationNotFound() {
        // given
        when(conversationRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            reviewService.createReview(reviewer, requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("대화 요청을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 수락되지 않은 대화")
    void createReview_Fail_ConversationNotAccepted() {
        // given
        conversationRequest.updateStatus(RequestStatus.PENDING);
        when(conversationRequestRepository.findById(1L)).thenReturn(Optional.of(conversationRequest));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(reviewer, requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("수락된 대화에 대해서만 리뷰를 작성할 수 있습니다.");
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 중복 리뷰")
    void createReview_Fail_DuplicateReview() {
        // given
        when(conversationRequestRepository.findById(1L)).thenReturn(Optional.of(conversationRequest));
        when(reviewRepository.existsByConversationRequestAndReviewer(conversationRequest, reviewer)).thenReturn(true);

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(reviewer, requestDto);
        });
        assertThat(exception.getMessage()).isEqualTo("이미 해당 대화에 대한 리뷰를 작성했습니다.");
    }
}
