package soulfit.soulfit.matching.swipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.swipe.domain.Match;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByUser1AndUser2(UserAuth user1, UserAuth user2);
    Optional<Match> findByUser2AndUser1(UserAuth user2, UserAuth user1);
}
