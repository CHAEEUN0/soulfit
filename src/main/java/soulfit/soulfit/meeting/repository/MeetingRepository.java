package soulfit.soulfit.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.meeting.domain.Meeting;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {




}
