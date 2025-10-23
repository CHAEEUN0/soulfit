package soulfit.soulfit.chat;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDto {
    private Long roomId;
    private String roomName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
    private String imageUrl;
    private Integer opponentId;
}