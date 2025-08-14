package soulfit.soulfit.chat;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketIntegrationTest {


    private Long roomId;


    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @BeforeEach
    void setUp() throws Exception {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom = chatRoomRepository.save(chatRoom);
        roomId = chatRoom.getId();

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://localhost:8080/ws";
        //String url = "wss://localhost:8443/ws";
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzU0NDU0ODczLCJleHAiOjE3NTQ1NDEyNzN9.Bm1RDjwNapxC5yfrPWQ5cHwico1KtGZ-R7WbQHF_05w");

        this.stompSession = stompClient.connect(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
        }).get(3, TimeUnit.SECONDS);
    }


    @Test
    void sendAndReceiveChatMessage() throws Exception {

        BlockingQueue<ChatMessageResponseDto> queue = new LinkedBlockingQueue<>();
        stompSession.subscribe("/topic/room/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponseDto.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((ChatMessageResponseDto) payload);
            }
        });



        ChatMessageRequestDto dto = ChatMessageRequestDto.builder()
                .roomId(roomId)
                .message("Hello")
                .build();

        stompSession.send("/app/chat/send", dto);


        ChatMessageResponseDto received = queue.poll(2, TimeUnit.SECONDS);
        assertThat(received.getMessage()).isEqualTo("Hello");
        assertThat(received).isNotNull();

        System.out.println("createdAt: " + received.getCreatedAt());
        System.out.println("displayTime: " + received.getDisplayTime());
    }
}
