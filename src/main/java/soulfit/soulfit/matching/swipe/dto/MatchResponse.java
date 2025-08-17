package soulfit.soulfit.matching.swipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchResponse {
    private boolean isMatch;
    private Long createdChatRoomId;
}
