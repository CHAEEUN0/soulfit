package soulfit.soulfit.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.meeting.domain.MeetingQuestion;

import java.util.List;

public interface MeetingQuestionRepository extends JpaRepository<MeetingQuestion, Long> {
    List<MeetingQuestion> findByMeetingId(Long meetingId);
}
