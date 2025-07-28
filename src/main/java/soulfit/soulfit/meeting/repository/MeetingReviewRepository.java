package soulfit.soulfit.meeting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.meeting.domain.MeetingReview;

@Repository
public interface MeetingReviewRepository extends JpaRepository<MeetingReview, Long> {

    Page<MeetingReview> findByMeetingId(Long meetingId, Pageable pageable);
}
