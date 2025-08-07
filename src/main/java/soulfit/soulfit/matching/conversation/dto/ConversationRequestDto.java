package soulfit.soulfit.matching.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConversationRequestDto(
    @NotNull(message = "요청을 받는 사용자 ID는 필수입니다.")
    Long toUserId,

    @NotBlank(message = "메시지는 필수입니다.")
    @Size(max = 255, message = "메시지는 최대 255자까지 입력할 수 있습니다.")
    String message
) {
}
