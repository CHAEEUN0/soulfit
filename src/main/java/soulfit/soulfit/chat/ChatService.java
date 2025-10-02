package soulfit.soulfit.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.chat.ai.AIAnalysisService;
import soulfit.soulfit.common.S3Uploader;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingImage;
import soulfit.soulfit.meeting.domain.MeetingParticipant;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MeetingRepository meetingRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final AIAnalysisService aiAnalysisService;


    @Transactional(readOnly = true)
    public Page<ChatRoomListDto> getMyRooms(UserAuth user, Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomRepository.findMyRoom(user, pageable);

        return chatRooms.map(room -> {
            ChatMessage lastMessage = chatMessageRepository
                    .findFirstByChatRoomOrderBySeqDesc(room)
                    .orElse(null);

            ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(room, user)
                    .orElseThrow(() -> new IllegalStateException("참가자가 아님"));

            long unreadCount = Math.max(0, room.getLastSeq() - chatParticipant.getLastReadSeq());

            return new ChatRoomListDto(
                    room.getId(),
                    room.getName(),
                    lastMessage != null ? lastMessage.getMessage() : null,
                    room.getUpdatedAt()
                    , unreadCount);
        });
    }


    @Transactional(readOnly = true)
    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }


    @Transactional
    public Long getOrCreateChatRoom(Long otherUserId, UserAuth user) {
        UserAuth otherUser = userRepository.findById(otherUserId).orElseThrow(() -> new RuntimeException("user not found: " + user.getId()));

        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(user.getId(), otherUser.getId());
        if (chatRoom.isPresent()) {
            return chatRoom.get().getId();
        }


        ChatRoom newRoom = ChatRoom.builder()
                .type(ChatRoomType.Direct)
                .build();
        chatRoomRepository.save(newRoom);


        addParticipantToRoom(newRoom, user);
        addParticipantToRoom(newRoom, otherUser);

        return newRoom.getId();

    }

    @Transactional
    public ChatRoom createGroupChatRoom(Long meetingId, UserAuth host) {

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("meeting not found"));

        if (!meeting.getHost().getId().equals(host.getId())) {
            throw new RuntimeException("host 아님");
        }


        return chatRoomRepository.findByMeeting(meeting)
                .orElseGet(() -> {
                    ChatRoom chatRoom = ChatRoom.builder()
                            .name(meeting.getTitle())
                            .type(ChatRoomType.GROUP)
                            .meeting(meeting)
                            .build();
                    chatRoomRepository.save(chatRoom);
                    addParticipantToRoom(chatRoom, host);
                    return chatRoom;
                });
    }


    @Transactional
    public void joinGroup(Long meetingId, UserAuth user) {

        ChatRoom chatRoom = chatRoomRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new RuntimeException("room not found"));

        MeetingParticipant meetingParticipant = meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for meeting: " + meetingId + " and user: " + user.getId()));

        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user);
        if (participant.isEmpty()) {
            addParticipantToRoom(chatRoom, user);
        }
        ;

    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessages(Long roomId, UserAuth user, Pageable pageable) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다 " + roomId));

        chatParticipantRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new RuntimeException("참여하지 않는 채팅방입니다."));

        return chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageable);
    }

    @Transactional
    public void leaveRoom(Long roomId, UserAuth user) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("room cannot be found"));

        ChatParticipant c = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new RuntimeException("참여자를 찾을 수 없습니다."));

        chatParticipantRepository.delete(c);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if (chatParticipants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    @Transactional
    public ChatMessage sendMessage(ChatMessageRequestDto dto) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("room not found " + dto.getRoomId()));

        long next = chatRoom.getLastSeq() + 1;
        chatRoom.setLastSeq(next);

        ChatMessage chatMessage = dto.toEntity();
        chatMessage.setSeq(next);
        chatRoom.addMessage(chatMessage);

        aiAnalysisService.analyzeConversationAndBroadcast(dto.getRoomId());

        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public ChatMessage sendImages(Long roomId, ChatImageRequestDto dto, UserAuth user) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("room not found"));

        long next = chatRoom.getLastSeq() + 1;
        chatRoom.setLastSeq(next);

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(user.getUsername())
                .seq(next)
                .build();

        chatRoom.addMessage(chatMessage);

        List<ChatImage> chatImages = new ArrayList<>();
        List<MultipartFile> images = dto.getImages();
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                String key = createKeyName(image.getOriginalFilename());
                String url = s3Uploader.upload(image, key);

                chatImages.add(ChatImage.builder()
                        .chatMessage(chatMessage)
                        .order(i)
                        .imageUrl(url)
                        .imageKey(key)
                        .build());
            }
            chatMessage.getImages().addAll(chatImages);
        }
        return chatMessageRepository.save(chatMessage);
    }


    private String createKeyName(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "chat/" + UUID.randomUUID() + ext;
    }


    private void addParticipantToRoom(ChatRoom chatRoom, UserAuth user) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();

        chatParticipantRepository.save(chatParticipant);
    }

}
