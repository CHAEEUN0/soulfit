package soulfit.soulfit.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    private UserAuth userA;
    private UserAuth userB;

    @BeforeEach
    void setUp() {
        userA = new UserAuth("userA", "password", "userA@test.com");
        userB = new UserAuth("userB", "password", "userB@test.com");
        userRepository.save(userA);
        userRepository.save(userB);
    }

    @Test
    @DisplayName("1:1 채팅방 목록 조회 시, 채팅방 이름이 상대방 이름으로 설정되어야 한다")
    void getMyRooms_forDirectChat_returnsOpponentName() {
        // given
        // 1:1 채팅방 생성
        ChatRoom directRoom = ChatRoom.builder()
                .type(ChatRoomType.Direct)
                .build();
        chatRoomRepository.save(directRoom);

        // 채팅방에 참여자 추가
        ChatParticipant participantA = ChatParticipant.builder().chatRoom(directRoom).user(userA).build();
        ChatParticipant participantB = ChatParticipant.builder().chatRoom(directRoom).user(userB).build();
        chatParticipantRepository.save(participantA);
        chatParticipantRepository.save(participantB);

        // when
        // userA 입장에서 채팅방 목록 조회
        Page<ChatRoomListDto> result = chatService.getMyRooms(userA, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        ChatRoomListDto chatRoomDto = result.getContent().get(0);
        assertThat(chatRoomDto.getRoomName()).isEqualTo(userB.getUsername());
    }

    @Test
    @DisplayName("그룹 채팅방 목록 조회 시, 채팅방 이름이 기존 설정된 이름으로 유지되어야 한다")
    void getMyRooms_forGroupChat_returnsRoomName() {
        // given
        // 그룹 채팅방 생성
        String groupChatName = "Test Group Chat";
        ChatRoom groupRoom = ChatRoom.builder()
                .type(ChatRoomType.GROUP)
                .name(groupChatName)
                .build();
        chatRoomRepository.save(groupRoom);

        // 채팅방에 참여자 추가
        ChatParticipant participantA = ChatParticipant.builder().chatRoom(groupRoom).user(userA).build();
        chatParticipantRepository.save(participantA);

        // when
        // userA 입장에서 채팅방 목록 조회
        Page<ChatRoomListDto> result = chatService.getMyRooms(userA, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        ChatRoomListDto chatRoomDto = result.getContent().get(0);
        assertThat(chatRoomDto.getRoomName()).isEqualTo(groupChatName);
    }
}
