package soulfit.soulfit.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomDto {

    private Long roomId;
    private String roomName; // 1:1에서는 상대 닉네임, 모임 단톡에서는 모임 이름

    public static ChatRoomDto from(ChatRoom chatRoom){
        return ChatRoomDto.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .build();
    }
}
