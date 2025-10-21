package soulfit.soulfit.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.chat.ai.AiChatAnalysisResponseDto;
import soulfit.soulfit.chat.ai.AiChatService;
import soulfit.soulfit.chat.ai.AiRecommendResponseDto;
import java.security.Principal;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final AiChatService aiChatService;
    private final SimpMessagingTemplate messagingTemplate;


    @GetMapping("/rooms")
    public List<ChatRoomDto> getAllChatRooms() {
        return chatService.getAllRooms().stream().map(ChatRoomDto::from).toList();
    }


    // 내채팅방
    @GetMapping("/rooms/my")
    public Page<ChatRoomListDto> getMyRooms(@AuthenticationPrincipal UserAuth user, Pageable pageable) {
        return chatService.getMyRooms(user, pageable);
    }


    @GetMapping("/rooms/{roomId}/messages")
    public Page<ChatMessageResponseDto> getMessages(@PathVariable Long roomId, @AuthenticationPrincipal UserAuth user, Pageable pageable) {
        return chatService.getMessages(roomId, user, pageable).map(ChatMessageResponseDto::from);
    }

    //개인채팅(소개팅, 번개)
    @PostMapping("/rooms/direct")
    public ResponseEntity<Long> getOrCreateDirectRoom(@RequestParam Long otherUserId, @AuthenticationPrincipal UserAuth user) {
        Long roomId = chatService.getOrCreateChatRoom(otherUserId, user);
        return ResponseEntity.ok(roomId);
    }

    @PostMapping("/{roomId}/read")
    public ResponseEntity<Void> readMessages(@PathVariable Long roomId, @AuthenticationPrincipal UserAuth user) {
        log.info("Request from user: {} to mark all messages as read for room: {}", user.getUsername(), roomId);
        chatService.readMessages(roomId, user);
        return ResponseEntity.ok().build();
    }


    // AI 대화 분석
    @GetMapping("/rooms/{roomId}/messages/analysis")
    public ResponseEntity<AiChatAnalysisResponseDto> analyzeChat(@PathVariable Long roomId, @AuthenticationPrincipal UserAuth user) {
        // 분석할 메세지들
        List<ChatMessage> messages = chatService.getRecentMessages(roomId);
        AiChatAnalysisResponseDto response = aiChatService.analyzeChat(messages);
        return ResponseEntity.ok(response);
    }


    // AI 메세지 추천
    @GetMapping("/rooms/{roomId}/messages/recommend")
    public ResponseEntity<AiRecommendResponseDto> recommendChat(@PathVariable Long roomId, @AuthenticationPrincipal UserAuth user) {

        //내가 마지막으로 보낸 메세지 이후 상대의 답장들
        Long lastMyMessageId = chatService.getLastMyMessageId(roomId, user.getUsername());

        if (lastMyMessageId == null) {
            return ResponseEntity.badRequest().build(); // 답장 없으면 추천 불가
        }

        List<ChatMessage> replies =
                chatService.getMessagesAfter(roomId, user.getUsername(), lastMyMessageId);


        AiRecommendResponseDto response = aiChatService.recommendChat(replies);
        return ResponseEntity.ok(response);
    }




    //단체(모임용)
    @PostMapping("/rooms/meetings/{meetingId}")
    public ResponseEntity<ChatRoomDto> createGroupRoom(@PathVariable Long meetingId, @AuthenticationPrincipal UserAuth user) {
        ChatRoom saved = chatService.createGroupChatRoom(meetingId, user);
        return ResponseEntity.ok(ChatRoomDto.from(saved));
    }

    //모임 채팅방 입장
    @PostMapping("/rooms/meetings/{meetingId}/join")
    public ResponseEntity<Void> joinGroupRoom(@PathVariable Long meetingId, @AuthenticationPrincipal UserAuth user) {
        chatService.joinGroup(meetingId, user);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> leaveChatRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserAuth user) {
        chatService.leaveRoom(roomId, user);
        return ResponseEntity.noContent().build();
    }


    @MessageMapping("/chat/send")
    public void sendMessage(@Valid ChatMessageRequestDto dto, @AuthenticationPrincipal Principal principal) {
        String username = principal.getName();
        dto.setSender(username);

        // 1. 메시지 저장 및 전송
        ChatMessage chatMessage = chatService.sendMessage(dto);
        messagingTemplate.convertAndSend("/topic/room/" + dto.getRoomId(), ChatMessageResponseDto.from(chatMessage));
    }


    @PostMapping(
            value = "/rooms/{roomId}images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ChatMessageResponseDto> sendImages(@PathVariable Long roomId, @ModelAttribute @Valid ChatImageRequestDto dto,
                                                             @AuthenticationPrincipal UserAuth user) {

        ChatMessage saved = chatService.sendImages(roomId, dto, user);
        ChatMessageResponseDto responseDto = ChatMessageResponseDto.from(saved);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, responseDto);
        return ResponseEntity.ok(responseDto);
    }
}
