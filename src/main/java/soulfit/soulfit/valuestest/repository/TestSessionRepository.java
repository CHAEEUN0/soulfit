package soulfit.soulfit.valuestest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.valuestest.domain.SessionStatus;
import soulfit.soulfit.valuestest.domain.TestSession;
import soulfit.soulfit.valuestest.domain.TestType;

import java.util.List;
import java.util.Optional;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    List<TestSession> findByUserId(Long userId);

    List<TestSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    Optional<TestSession> findFirstByUserIdAndTestTypeAndStatusOrderBySubmittedAtDesc(
            Long userId, TestType testType, SessionStatus status);

}
