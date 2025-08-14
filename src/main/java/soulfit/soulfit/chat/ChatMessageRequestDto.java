package soulfit.soulfit.chat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {
    @NotNull
    private Long roomId;
    //닉네임
    private String sender;
    @NotEmpty
    private String message;

    public ChatMessage toEntity(){
        return ChatMessage.builder()
                .sender(sender)
                .message(message)
                .build();
    }

}
