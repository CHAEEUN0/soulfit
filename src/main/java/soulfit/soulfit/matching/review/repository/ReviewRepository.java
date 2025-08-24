
package soulfit.soulfit.matching.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.review.domain.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeId(Long revieweeId);

    List<Review> findByReviewerId(Long reviewerId);

    boolean existsByConversationRequestAndReviewer(ConversationRequest conversationRequest, UserAuth reviewer);
}
