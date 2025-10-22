package soulfit.soulfit.chat.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import soulfit.soulfit.chat.ChatMessage;
import soulfit.soulfit.chat.ai.client.AiChatClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final AiChatClient aiChatClient;
    private final SimpMessagingTemplate messagingTemplate;

    public AiChatAnalysisResponseDto analyzeChat(List<ChatMessage> chatMessages) {

        Long chatRoomId = chatMessages.isEmpty() ? null :
                chatMessages.get(0).getChatRoom().getId();

        log.info("Requesting detailed AI chat analysis for room: {}", chatRoomId);

        List<AiChatAnalysisRequestDto.MessageData> messageDataList = chatMessages.stream()
                .filter(chatMessage -> chatMessage.getMessage() != null && !chatMessage.getMessage().isBlank())
                .map(chatMessage -> new AiChatAnalysisRequestDto.MessageData(
                        chatMessage.getSender(),
                        chatMessage.getMessage(),
                        chatMessage.getCreatedAt().toString()
                ))
                .toList();

        AiChatAnalysisRequestDto requestDto = new AiChatAnalysisRequestDto(
                chatRoomId != null ? chatRoomId.toString() : null, messageDataList
        );
        log.debug("AI analysis request for room {}: {}", chatRoomId, requestDto);

        AiChatAnalysisResponseDto response = aiChatClient.analyzeMessage(requestDto);
        log.debug("Detailed AI analysis response for room {}: {}", chatRoomId, response);
        return response;
    }

    @Async
    public void analyzeAndBroadcast(List<ChatMessage> chatMessages) {
        if (chatMessages.isEmpty()) {
            return;
        }
        Long roomId = chatMessages.get(0).getChatRoom().getId();
        log.info("Starting real-time detailed AI analysis for room: {}", roomId);

        // 기존 분석 로직을 호출하여 결과를 얻음
        AiChatAnalysisResponseDto response = analyzeChat(chatMessages);

        if (response != null) {
            // 얻은 결과를 WebSocket 토픽으로 전송
            messagingTemplate.convertAndSend("/topic/analysis/" + roomId, response);
            log.info("Successfully broadcasted detailed AI analysis for room: {}", roomId);
        }
    }

    public AiRecommendResponseDto recommendChat(List<ChatMessage> chatMessages) {

        Long chatRoomId = chatMessages.isEmpty() ? null :
                chatMessages.get(0).getChatRoom().getId();
        log.info("Requesting AI chat recommendation for room: {}", chatRoomId);

        List<AiRecommendRequestDto.MessageData> messageDataList = chatMessages.stream()
                .map(chatMessage -> new AiRecommendRequestDto.MessageData(
                        chatMessage.getSender(),
                        chatMessage.getMessage(),
                        chatMessage.getCreatedAt().toString()
                ))
                .toList();

        AiRecommendRequestDto requestDto = new AiRecommendRequestDto(
                chatRoomId != null ? chatRoomId.toString() : null, messageDataList
        );
        log.debug("AI recommendation request for room {}: {}", chatRoomId, requestDto);

        AiRecommendResponseDto response = aiChatClient.recommendChat(requestDto);
        log.debug("AI recommendation response for room {}: {}", chatRoomId, response);
        return response;
    }
}
