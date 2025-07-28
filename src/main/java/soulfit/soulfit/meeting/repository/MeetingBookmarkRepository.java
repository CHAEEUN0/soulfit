package soulfit.soulfit.meeting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingBookmark;

import java.util.Optional;

@Repository
public interface MeetingBookmarkRepository extends JpaRepository<MeetingBookmark, Long> {

    Optional<MeetingBookmark> findByMeetingAndUser(Meeting meeting, UserAuth userAuth);
    Page<MeetingBookmark> findByUserOrderByCreatedAtDesc(UserAuth user, Pageable pageable);
}
