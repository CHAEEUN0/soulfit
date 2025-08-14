package soulfit.soulfit.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long id;
    private Long roomId;
    //닉네임
    private String sender;
    private String message;
    private String createdAt;
    private String displayTime;
    private List<String> imageUrls;

    public static ChatMessageResponseDto from(ChatMessage chatMessage){
        List<String> imageUrls = chatMessage.getImages().stream()
                .map(ChatImage::getImageUrl)
                .toList();

        return ChatMessageResponseDto.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getChatRoom().getId())
                .sender(chatMessage.getSender())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt().toString())
                .imageUrls(imageUrls)
                .displayTime(chatMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA)))
                .build();
    };


}
