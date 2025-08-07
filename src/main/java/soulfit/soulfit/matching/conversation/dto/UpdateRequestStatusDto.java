package soulfit.soulfit.matching.conversation.dto;

import jakarta.validation.constraints.NotNull;
import soulfit.soulfit.matching.conversation.validation.ValidRequestStatus;

public record UpdateRequestStatusDto(
    @NotNull(message = "상태 값은 필수입니다.")
    @ValidRequestStatus
    String status
) {
}
