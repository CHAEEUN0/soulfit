
package soulfit.soulfit.matching.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.review.domain.Review;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeId(Long revieweeId);

    List<Review> findByReviewerId(Long reviewerId);

    boolean existsByConversationRequestAndReviewer(ConversationRequest conversationRequest, UserAuth reviewer);

    @Query("SELECT rk.keyword FROM Review r JOIN r.keywords rk WHERE r.reviewee.id = :userId GROUP BY rk.keyword ORDER BY COUNT(rk.keyword) DESC")
    List<String> findTopKeywordsByRevieweeId(@Param("userId") Long userId, Pageable pageable);
}
