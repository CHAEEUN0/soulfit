package soulfit.soulfit.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.meeting.domain.MeetingParticipant;

import java.util.List;
import java.util.Optional;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);
    List<MeetingParticipant> findByMeetingId(Long meetingId);
}
