package soulfit.soulfit.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.meeting.domain.MeetingAnswer;

import java.util.List;

public interface MeetingAnswerRepository extends JpaRepository<MeetingAnswer, Long> {
    List<MeetingAnswer> findByParticipantMeetingId(Long meetingId);
    List<MeetingAnswer> findByParticipantMeetingIdAndParticipantUserId(Long meetingId, Long userId);
}
