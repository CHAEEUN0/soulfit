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
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Location;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingImage;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.profile.domain.UserProfile;

import java.time.LocalDateTime;
import java.util.ArrayList;

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

    @Autowired
    private MeetingRepository meetingRepository;

    private UserAuth userA;
    private UserAuth userB;

    @BeforeEach
    void setUp() {
        userA = new UserAuth("userA", "password", "userA@test.com");
        userB = new UserAuth("userB", "password", "userB@test.com");
    }

    @Test
    @DisplayName("1:1 채팅방 목록 조회 시, 채팅방 이름은 상대방 이름, 이미지는 상대방 프로필 이미지여야 한다")
    void getMyRooms_forDirectChat_returnsOpponentNameAndImage() {
        // given
        userRepository.save(userA);

        String opponentProfileUrl = "http://example.com/profile/userB.jpg";
        UserProfile userBProfile = new UserProfile();
        userBProfile.setProfileImageUrl(opponentProfileUrl);
        userB.setUserProfile(userBProfile);
        userRepository.save(userB);

        ChatRoom directRoom = ChatRoom.builder()
                .type(ChatRoomType.Direct)
                .build();
        chatRoomRepository.save(directRoom);

        ChatParticipant participantA = ChatParticipant.builder().chatRoom(directRoom).user(userA).build();
        ChatParticipant participantB = ChatParticipant.builder().chatRoom(directRoom).user(userB).build();
        chatParticipantRepository.save(participantA);
        chatParticipantRepository.save(participantB);

        // when
        Page<ChatRoomListDto> result = chatService.getMyRooms(userA, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        ChatRoomListDto chatRoomDto = result.getContent().get(0);
        assertThat(chatRoomDto.getRoomName()).isEqualTo(userB.getUsername());
        assertThat(chatRoomDto.getImageUrl()).isEqualTo(opponentProfileUrl);
    }

    @Test
    @DisplayName("그룹 채팅방 목록 조회 시, 채팅방 이름은 기존 이름, 이미지는 모임의 대표 이미지여야 한다")
    void getMyRooms_forGroupChat_returnsRoomNameAndMeetingImage() {
        // given
        userRepository.save(userA);

        String meetingImageUrl = "http://example.com/meeting/image.jpg";
        Meeting meeting = Meeting.builder()
                .host(userA)
                .title("Test Meeting")
                .description("desc")
                .category(Category.HOBBY)
                .location(Location.builder().city("서울").build())
                .fee(0)
                .meetingTime(LocalDateTime.now().plusDays(1))
                .build();

        MeetingImage meetingImage = MeetingImage.builder()
                .meeting(meeting)
                .imageUrl(meetingImageUrl)
                .order(0)
                .build();
        meeting.getImages().add(meetingImage);
        meetingRepository.save(meeting);

        String groupChatName = "Test Group Chat";
        ChatRoom groupRoom = ChatRoom.builder()
                .type(ChatRoomType.GROUP)
                .name(groupChatName)
                .meeting(meeting)
                .build();
        chatRoomRepository.save(groupRoom);

        ChatParticipant participantA = ChatParticipant.builder().chatRoom(groupRoom).user(userA).build();
        chatParticipantRepository.save(participantA);

        // when
        Page<ChatRoomListDto> result = chatService.getMyRooms(userA, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        ChatRoomListDto chatRoomDto = result.getContent().get(0);
        assertThat(chatRoomDto.getRoomName()).isEqualTo(groupChatName);
        assertThat(chatRoomDto.getImageUrl()).isEqualTo(meetingImageUrl);
    }
}
