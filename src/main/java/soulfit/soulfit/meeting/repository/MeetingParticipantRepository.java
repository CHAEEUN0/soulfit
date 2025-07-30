package soulfit.soulfit.meeting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingParticipant;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

    List<MeetingParticipant> findByMeetingId(Long meetingId);


    @Query("SELECT mp.meeting FROM MeetingParticipant mp " +
            "WHERE mp.user = :user " +
            "AND mp.approvalStatus = 'APPROVED' " +
            "AND mp.meeting.meetingStatus = 'FINISHED'")
    Page<Meeting> findMeetingUserParticipated(@Param("user") UserAuth user, Pageable pageable);

}
