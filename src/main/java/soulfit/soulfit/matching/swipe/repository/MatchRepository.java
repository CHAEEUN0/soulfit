package soulfit.soulfit.matching.swipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.matching.swipe.domain.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
