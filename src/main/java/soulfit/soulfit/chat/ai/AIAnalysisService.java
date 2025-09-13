package soulfit.soulfit.chat.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import soulfit.soulfit.chat.ChatMessage;
import soulfit.soulfit.chat.ChatMessageRepository;
import soulfit.soulfit.chat.ai.client.AiChatClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AiChatClient aiChatClient;

    @Async
    public void analyzeConversationAndBroadcast(Long roomId) {
        log.info("Starting AI analysis for room: {}", roomId);

        List<String> recentMessages = chatMessageRepository.findTop20ByChatRoomIdOrderByCreatedAtDesc(roomId)
                .stream()
                .map(ChatMessage::getMessage)
                .collect(Collectors.toList());

        if (recentMessages.isEmpty()) {
            log.info("No messages to analyze for room: {}", roomId);
            return;
        }

        AIAnalysisResponseDto response = aiChatClient.analyzeChat(recentMessages);

        if (response != null) {
            log.info("AI analysis successful for room: {}. Mood: {}", roomId, response.getMood());
            broadcastAnalysis(roomId, response);
        }
    }

    private void broadcastAnalysis(Long roomId, AIAnalysisResponseDto analysisResult) {
        ChatAnalysisDto clientDto = ChatAnalysisDto.from(analysisResult);
        messagingTemplate.convertAndSend("/topic/analysis/" + roomId, clientDto);
    }
}
