package soulfit.soulfit.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.test.domain.SessionStatus;
import soulfit.soulfit.test.domain.TestSession;
import soulfit.soulfit.test.domain.TestType;

import java.util.List;
import java.util.Optional;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    List<TestSession> findByUserId(Long userId);

    List<TestSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    Optional<TestSession> findFirstByUserIdAndTestTypeAndStatusOrderBySubmittedAtDesc(
            Long userId, TestType testType, SessionStatus status);

}
