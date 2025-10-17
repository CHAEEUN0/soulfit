package soulfit.soulfit.chat.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.chat.ChatMessage;
import soulfit.soulfit.chat.ai.client.AiChatClient;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatClient aiChatClient;

    public AiChatAnalysisResponseDto analyzeChat(List<ChatMessage> chatMessages) {

        Long chatRoomId = chatMessages.isEmpty() ? null :
                chatMessages.get(0).getChatRoom().getId();

        List<AiChatAnalysisRequestDto.MessageData> messageDataList = chatMessages.stream()
                .filter(chatMessage -> chatMessage.getMessage() !=null && chatMessage.getMessage().isBlank())
                .map(chatMessage -> new AiChatAnalysisRequestDto.MessageData(
                        chatMessage.getSender(),
                        chatMessage.getMessage(),
                        chatMessage.getCreatedAt().toString()
                ))
                .toList();

        AiChatAnalysisRequestDto requestDto = new AiChatAnalysisRequestDto(chatRoomId, messageDataList);

        return aiChatClient.analyzeMessage(requestDto);
    }

    public AiRecommendResponseDto recommendChat(List<ChatMessage> chatMessages) {

        Long chatRoomId = chatMessages.isEmpty() ? null :
                chatMessages.get(0).getChatRoom().getId();

        List<AiRecommendRequestDto.MessageData> messageDataList = chatMessages.stream()
                .map(chatMessage -> new AiRecommendRequestDto.MessageData(
                        chatMessage.getSender(),
                        chatMessage.getMessage(),
                        chatMessage.getCreatedAt().toString()
                ))
                .toList();

        AiRecommendRequestDto requestDto = new AiRecommendRequestDto(chatRoomId, messageDataList);

        return aiChatClient.recommendChat(requestDto);
    }
}
