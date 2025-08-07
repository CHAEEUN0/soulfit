package soulfit.soulfit.matching.conversation.dto;

import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;

import java.time.LocalDateTime;

public record ConversationResponseDto(
    Long id,
    Long fromUserId,
    String fromUserNickname, // 보낸 사람의 닉네임 추가
    Long toUserId,
    String message,
    RequestStatus status,
    LocalDateTime createdAt
) {
    public static ConversationResponseDto from(ConversationRequest request) {
        return new ConversationResponseDto(
            request.getId(),
            request.getFromUser().getId(),
            // UserAuth에 getNickname() 또는 유사한 메서드가 있다고 가정
            // 실제로는 UserProfile 등에서 가져와야 할 수 있음
            request.getFromUser().getUsername(),
            request.getToUser().getId(),
            request.getMessage(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}
