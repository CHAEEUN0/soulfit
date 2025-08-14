package soulfit.soulfit.matching.conversation.dto;

import soulfit.soulfit.matching.conversation.domain.RequestStatus;

import java.time.LocalDateTime;

public record ConversationResponseDto(
        Long id,
        ConversationPartnerDto fromUser,
        ConversationPartnerDto toUser,
        String message,
        RequestStatus status,
        LocalDateTime createdAt
) {
    public record ConversationPartnerDto(
            Long userId,
            String nickname,
            int age,
            String profileImageUrl
    ) {
    }
}
