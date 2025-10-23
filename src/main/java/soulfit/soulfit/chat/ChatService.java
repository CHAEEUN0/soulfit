package soulfit.soulfit.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.chat.ai.AIAnalysisService;
import soulfit.soulfit.chat.ai.AiChatService;
import soulfit.soulfit.common.S3Uploader;
import soulfit.soulfit.meeting.domain.MeetingImage;
import soulfit.soulfit.meeting.domain.MeetingParticipant;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.util.*;

@Slf4j
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
    private final AiChatService aiChatService;


    public List<ChatMessage> getRecentMessages(Long roomId) {
        List<ChatMessage> recent = chatMessageRepository
                .findTop50ByChatRoomIdOrderByCreatedAtDesc(roomId);

        Collections.reverse(recent);

        return recent;
    }

    public Long getLastMyMessageId(Long chatRoomId, String myName) {
        return chatMessageRepository
                .findTopByChatRoomIdAndSenderOrderByCreatedAtDesc(chatRoomId, myName)
                .map(ChatMessage::getId)
                .orElse(null);
    }

    public List<ChatMessage> getMessagesAfter(Long chatRoomId, String sender, Long lastMessageId) {
        return chatMessageRepository
                .findByChatRoomIdAndIdGreaterThanAndSenderNotOrderByCreatedAtAsc(
                        chatRoomId,
                        lastMessageId,
                        sender
                );
    }

    @Transactional
    public void readMessages(Long roomId, UserAuth user) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("room not found"));

        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new IllegalStateException("참가자가 아님"));

        log.debug("Updating lastReadSeq for user: {} in room: {}. New lastReadSeq: {}", user.getUsername(), roomId, chatRoom.getLastSeq());
        chatParticipant.updateLastReadSeq(chatRoom.getLastSeq());
    }

    @Transactional(readOnly = true)
    public Page<ChatRoomListDto> getMyRooms(UserAuth user, Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomRepository.findMyRoom(user, pageable);

        return chatRooms.map(room -> {
            ChatMessage lastMessage = chatMessageRepository
                    .findFirstByChatRoomOrderBySeqDesc(room)
                    .orElse(null);

            ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(room, user)
                    .orElseThrow(() -> new IllegalStateException("참가자가 아님"));

            long unreadCount = chatMessageRepository.countByChatRoomAndSenderNotAndSeqGreaterThan(room, user.getUsername(), chatParticipant.getLastReadSeq());
            log.debug("User: {}, Room: {}, Unread count: {}", user.getUsername(), room.getId(), unreadCount);

            String roomDisplayName;
            String roomImageUrl = null; // 이미지 URL 변수 초기화
            Integer opponentId = null; // opponentId 초기화

            if (room.getType() == ChatRoomType.Direct) {
                // 1:1 채팅방: 상대방 정보에서 이름과 프로필 이미지를 가져온다.
                List<ChatParticipant> participants =
                        chatParticipantRepository.findByChatRoom(room);
                UserAuth otherUser = participants.stream()
                        .map(ChatParticipant::getUser)
                        .filter(participantUser -> !participantUser.getId().equals(user.getId()))
                        .findFirst()
                        .orElse(null);

                if (otherUser != null) {
                    roomDisplayName = otherUser.getUsername();
                    opponentId = otherUser.getId().intValue(); // 상대방 ID 설정
                    if (otherUser.getUserProfile() != null) {
                        roomImageUrl = otherUser.getUserProfile().getProfileImageUrl();
                    }
                } else {
                    roomDisplayName = "(알 수 없음)";
                }
            } else { // GROUP 타입
                // 그룹 채팅방: 기존 방 이름과 모임의 대표 이미지를 가져온다.
                roomDisplayName = room.getName();
                if (room.getMeeting() != null) {
                    opponentId = room.getMeeting().getId().intValue(); // 미팅 ID 설정
                    if (room.getMeeting().getImages() != null && !room.getMeeting().getImages().isEmpty()) {
                        // order를 기준으로 정렬하여 첫 번째 이미지를 대표 이미지로 사용
                        roomImageUrl = room.getMeeting().getImages().stream()
                                .min(java.util.Comparator.comparing(soulfit.soulfit.meeting.domain.MeetingImage::getOrder))
                                .map(soulfit.soulfit.meeting.domain.MeetingImage::getImageUrl)
                                .orElse(null);
                    }
                }
            }

            return new ChatRoomListDto(
                    room.getId(),
                    roomDisplayName,
                    lastMessage != null ? lastMessage.getMessage() : null,
                    room.getUpdatedAt(),
                    unreadCount,
                    roomImageUrl, // DTO에 이미지 URL 추가
                    opponentId // opponentId 추가
            );
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

        List<ChatMessage> recentMessages = getRecentMessages(dto.getRoomId());
        aiChatService.analyzeAndBroadcast(recentMessages);

//        aiAnalysisService.analyzeConversationAndBroadcast(dto.getRoomId());

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
