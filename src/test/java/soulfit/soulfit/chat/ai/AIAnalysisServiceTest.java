package soulfit.soulfit.chat.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import soulfit.soulfit.chat.ChatMessage;
import soulfit.soulfit.chat.ChatMessageRepository;
import soulfit.soulfit.chat.ai.client.AiChatClient;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIAnalysisServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AiChatClient aiChatClient; // RestTemplate 대신 AiChatClient를 Mocking

    private AIAnalysisService aiAnalysisService;

    @BeforeEach
    void setUp() {
        // 새로운 생성자에 맞게 AiChatClient를 주입
        aiAnalysisService = new AIAnalysisService(chatMessageRepository, messagingTemplate, aiChatClient);
    }

    @Test
    @DisplayName("성공: 채팅 분석 및 결과 브로드캐스팅")
    void analyzeConversationAndBroadcast_Success() throws InterruptedException {
        // given
        Long roomId = 1L;
        List<ChatMessage> mockMessages = List.of(
                ChatMessage.builder().sender("user1").message("hello").build(),
                ChatMessage.builder().sender("user2").message("world").build()
        );
        AIAnalysisResponseDto mockResponse = new AIAnalysisResponseDto();
        mockResponse.setMood("HAPPY");
        mockResponse.setKeywords(List.of("#greeting"));

        when(chatMessageRepository.findTop20ByChatRoomIdOrderByCreatedAtDesc(roomId)).thenReturn(mockMessages);
        // aiChatClient의 동작을 Mocking
        when(aiChatClient.analyzeChat(any())).thenReturn(mockResponse);

        // when
        aiAnalysisService.analyzeConversationAndBroadcast(roomId);

        // then
        Thread.sleep(500); // @Async 비동기 처리 대기

        verify(chatMessageRepository, times(1)).findTop20ByChatRoomIdOrderByCreatedAtDesc(roomId);
        verify(aiChatClient, times(1)).analyzeChat(any()); // aiChatClient 호출 검증

        ArgumentCaptor<ChatAnalysisDto> dtoCaptor = ArgumentCaptor.forClass(ChatAnalysisDto.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/analysis/" + roomId), dtoCaptor.capture());

        ChatAnalysisDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getMood()).isEqualTo("HAPPY");
        assertThat(capturedDto.getKeywords()).contains("#greeting");
    }

    @Test
    @DisplayName("분석할 메시지가 없으면 AI 클라이언트를 호출하지 않음")
    void analyzeConversationAndBroadcast_NoMessages() throws InterruptedException {
        // given
        Long roomId = 2L;
        when(chatMessageRepository.findTop20ByChatRoomIdOrderByCreatedAtDesc(roomId)).thenReturn(Collections.emptyList());

        // when
        aiAnalysisService.analyzeConversationAndBroadcast(roomId);

        // then
        Thread.sleep(500);

        verify(chatMessageRepository, times(1)).findTop20ByChatRoomIdOrderByCreatedAtDesc(roomId);
        verify(aiChatClient, never()).analyzeChat(any()); // aiChatClient 미호출 검증
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("AI 클라이언트가 null 반환 시 브로드캐스팅하지 않음")
    void analyzeConversationAndBroadcast_AiClientReturnsNull() throws InterruptedException {
        // given
        Long roomId = 3L;
        List<ChatMessage> mockMessages = List.of(ChatMessage.builder().sender("user1").message("error test").build());

        when(chatMessageRepository.findTop20ByChatRoomIdOrderByCreatedAtDesc(roomId)).thenReturn(mockMessages);
        // AI 클라이언트가 null을 반환하는 경우(통신 실패 등)를 Mocking
        when(aiChatClient.analyzeChat(any())).thenReturn(null);

        // when
        aiAnalysisService.analyzeConversationAndBroadcast(roomId);

        // then
        Thread.sleep(500);

        verify(chatMessageRepository, times(1)).findTop20ByChatRoomIdOrderByCreatedAtDesc(roomId);
        verify(aiChatClient, times(1)).analyzeChat(any()); // aiChatClient 호출 검증
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class)); // messagingTemplate 미호출 검증
    }
}