package soulfit.soulfit.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByMeeting(Meeting meeting);

    Optional<ChatRoom> findByMeetingId(Long meetingId);


    @Query("select distinct c from ChatRoom c join c.chatParticipants cp where cp.user = :user order by c.lastSeq desc")
    Page<ChatRoom> findMyRoom(@Param("user") UserAuth user, Pageable pageable);

}
