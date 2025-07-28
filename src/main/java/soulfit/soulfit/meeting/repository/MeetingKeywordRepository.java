package soulfit.soulfit.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.meeting.domain.Keyword;

@Repository
public interface MeetingKeywordRepository extends JpaRepository<Keyword, Long> {
}
