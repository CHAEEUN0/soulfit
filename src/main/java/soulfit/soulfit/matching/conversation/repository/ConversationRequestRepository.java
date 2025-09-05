package soulfit.soulfit.matching.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;

import java.util.Optional;

import java.util.List;

public interface ConversationRequestRepository extends JpaRepository<ConversationRequest, Long> {

    @Query("SELECT cr FROM ConversationRequest cr " +
            "JOIN FETCH cr.fromUser fu JOIN FETCH fu.userProfile " +
            "JOIN FETCH cr.toUser tu JOIN FETCH tu.userProfile " +
            "WHERE cr.toUser = :user AND cr.status = :status " +
            "ORDER BY cr.createdAt DESC")
    List<ConversationRequest> findByToUserAndStatusWithProfiles(@Param("user") UserAuth user, @Param("status") RequestStatus status);

    @Query("SELECT cr FROM ConversationRequest cr " +
            "JOIN FETCH cr.fromUser fu JOIN FETCH fu.userProfile " +
            "JOIN FETCH cr.toUser tu JOIN FETCH tu.userProfile " +
            "WHERE cr.fromUser = :user AND cr.status = :status " +
            "ORDER BY cr.createdAt DESC")
    List<ConversationRequest> findByFromUserAndStatusWithProfiles(@Param("user") UserAuth user, @Param("status") RequestStatus status);


    List<ConversationRequest> findByToUserIdAndStatusOrderByCreatedAtDesc(Long toUserId, RequestStatus status);

    List<ConversationRequest> findByFromUserIdAndStatusOrderByCreatedAtDesc(Long fromUserId, RequestStatus status);

    /**
     * 보낸 사람과 받는 사람, 그리고 상태를 기준으로 대화 신청이 존재하는지 확인합니다.
     * PENDING 상태의 중복 신청을 방지하기 위해 사용됩니다.
     *
     * @param fromUserId 요청 보낸 사용자의 ID
     * @param toUserId 요청 받은 사용자의 ID
     * @param status 확인할 상태
     * @return 존재 여부
     */
    boolean existsByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, RequestStatus status);

    /**
     * 특정 요청 ID와 요청을 받은 사용자 ID를 기준으로 대화 신청 정보를 조회합니다.
     * 요청을 받은 사람만이 수락/거절을 할 수 있도록 검증하는 데 사용됩니다.
     *
     * @param id 대화 신청 ID
     * @param toUserId 요청 받은 사용자의 ID
     * @return Optional<ConversationRequest>
     */
    Optional<ConversationRequest> findByIdAndToUserId(Long id, Long toUserId);

    Optional<ConversationRequest> findByFromUserAndToUser(UserAuth fromUser, UserAuth toUser);
}