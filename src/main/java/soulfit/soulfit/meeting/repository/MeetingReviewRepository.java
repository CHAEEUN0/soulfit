package soulfit.soulfit.meeting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.MeetingReview;

@Repository
public interface MeetingReviewRepository extends JpaRepository<MeetingReview, Long> {
    Page<MeetingReview> findByMeetingId(Long meetingId, Pageable pageable);
    List<MeetingReview> findByMeetingId(Long meetingId);
    Page<MeetingReview> findByUser(UserAuth user, Pageable pageable);
    @Query("SELECT AVG(r.hostRating) FROM MeetingReview r WHERE r.meeting.host.id = :hostId")
    Double findAverageHostRatingByHostId(@Param("hostId") Long hostId);
}
