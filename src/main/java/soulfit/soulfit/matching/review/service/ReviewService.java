
package soulfit.soulfit.matching.review.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final UserRepository userRepository;
    private final ConversationRequestRepository conversationRequestRepository;

    @Transactional
    public ReviewResponseDto createReview(UserAuth reviewer, ReviewRequestDto requestDto) {
        if (Objects.equals(reviewer.getId(), requestDto.getRevieweeId())) {
            throw new IllegalArgumentException("자기 자신을 리뷰할 수 없습니다.");
        }

        ConversationRequest conversationRequest = conversationRequestRepository.findById(requestDto.getConversationRequestId())
                .orElseThrow(() -> new EntityNotFoundException("대화 요청을 찾을 수 없습니다."));

        // 대화 상태 및 참여자 검증
        validateConversation(conversationRequest, reviewer.getId(), requestDto.getRevieweeId());

        // 중복 리뷰 검증
        if (reviewRepository.existsByConversationRequestAndReviewer(conversationRequest, reviewer)) {
            throw new IllegalStateException("이미 해당 대화에 대한 리뷰를 작성했습니다.");
        }

        UserAuth reviewee = userRepository.findById(requestDto.getRevieweeId())
                .orElseThrow(() -> new EntityNotFoundException("리뷰 대상자를 찾을 수 없습니다."));

        Set<ReviewKeyword> keywords = reviewKeywordRepository.findByKeywordIn(requestDto.getKeywords());
        if (keywords.size() != requestDto.getKeywords().size()) {
            throw new IllegalArgumentException("존재하지 않는 키워드가 포함되어 있습니다.");
        }

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .conversationRequest(conversationRequest)
                .comment(requestDto.getComment())
                .keywords(keywords)
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDto.from(savedReview);
    }

    private void validateConversation(ConversationRequest conversation, Long reviewerId, Long revieweeId) {
        if (conversation.getStatus() != RequestStatus.ACCEPTED) {
            throw new IllegalStateException("수락된 대화에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        Long fromUserId = conversation.getFromUser().getId();
        Long toUserId = conversation.getToUser().getId();

        boolean isReviewerParticipant = reviewerId.equals(fromUserId) || reviewerId.equals(toUserId);
        boolean isRevieweeParticipant = revieweeId.equals(fromUserId) || revieweeId.equals(toUserId);

        if (!isReviewerParticipant || !isRevieweeParticipant) {
            throw new IllegalStateException("대화에 참여한 사용자만 리뷰를 작성할 수 있습니다.");
        }
    }

    public List<ReviewResponseDto> getReviewsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다.");
        }
        List<Review> reviews = reviewRepository.findByRevieweeId(userId);
        return reviews.stream().map(ReviewResponseDto::from).collect(Collectors.toList());
    }

    public List<ReviewResponseDto> getReviewsByMe(UserAuth reviewer) {
        List<Review> reviews = reviewRepository.findByReviewerId(reviewer.getId());
        return reviews.stream().map(ReviewResponseDto::from).collect(Collectors.toList());
    }

    public List<String> getAllKeywords() {
        return reviewKeywordRepository.findAll().stream()
                .map(ReviewKeyword::getKeyword)
                .collect(Collectors.toList());
    }

    public List<String> getTopKeywordsForUser(Long userId, int limit) {
        logger.info("Entering getTopKeywordsForUser for userId: {}", userId);
        try {
            if (!userRepository.existsById(userId)) {
                throw new EntityNotFoundException("사용자를 찾을 수 없습니다.");
            }
            Pageable pageable = PageRequest.of(0, limit);
            List<String> keywords = reviewRepository.findTopKeywordsByRevieweeId(userId, pageable);
            logger.info("Exiting getTopKeywordsForUser for userId: {} with {} keywords.", userId, keywords.size());
            return keywords;
        } catch (Exception e) {
            logger.error("Error in getTopKeywordsForUser for userId: {}", userId, e);
            throw e;
        }
    }
}
