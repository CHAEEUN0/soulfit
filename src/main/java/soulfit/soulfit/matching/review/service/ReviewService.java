
package soulfit.soulfit.matching.review.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.chat.ChatParticipant;
import soulfit.soulfit.chat.ChatParticipantRepository;
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
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional
    public ReviewResponseDto createReview(UserAuth reviewer, ReviewRequestDto requestDto) {
        if (Objects.equals(reviewer.getId(), requestDto.getRevieweeId())) {
            throw new IllegalArgumentException("자기 자신을 리뷰할 수 없습니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getChatRoomId())
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        // 리뷰어와 리뷰이가 채팅방 참가자인지 검증
        boolean reviewerIsParticipant = chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewer);
        UserAuth reviewee = userRepository.findById(requestDto.getRevieweeId())
                .orElseThrow(() -> new EntityNotFoundException("리뷰 대상자를 찾을 수 없습니다."));
        boolean revieweeIsParticipant = chatParticipantRepository.existsByChatRoomAndUser(chatRoom, reviewee);

        if (!reviewerIsParticipant || !revieweeIsParticipant) {
            throw new IllegalArgumentException("리뷰어 또는 리뷰 대상자가 해당 채팅방의 참가자가 아닙니다.");
        }

        // 중복 리뷰 검증
        if (reviewRepository.existsByChatRoomAndReviewer(chatRoom, reviewer)) {
            throw new IllegalStateException("이미 해당 채팅방에 대한 리뷰를 작성했습니다.");
        }

        Set<ReviewKeyword> keywords = reviewKeywordRepository.findByKeywordIn(requestDto.getKeywords());
        if (keywords.size() != requestDto.getKeywords().size()) {
            throw new IllegalArgumentException("존재하지 않는 키워드가 포함되어 있습니다.");
        }

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .chatRoom(chatRoom)
                .comment(requestDto.getComment())
                .keywords(keywords)
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResponseDto.from(savedReview);
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
