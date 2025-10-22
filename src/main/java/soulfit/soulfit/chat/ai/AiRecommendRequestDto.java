package soulfit.soulfit.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendRequestDto {

    private String chatRoomId;
    private List<MessageData> messages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageData{
        private String sender;
        private String text;
        private String timestamp;

    }
}
