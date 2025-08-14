package soulfit.soulfit.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, UserAuth user);

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id WHERE cp1.user.id = :myId AND cp2.user.id = :otherMemberId AND cp1.chatRoom.type = 'DIRECT'")
    Optional<ChatRoom> findExistingPrivateRoom (@Param("myId") Long myId, @Param("otherMemberId") Long otherUserId);

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
}

