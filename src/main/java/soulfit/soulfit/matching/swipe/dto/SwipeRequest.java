package soulfit.soulfit.matching.swipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import soulfit.soulfit.matching.swipe.domain.SwipeType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SwipeRequest {
    private Long swipedUserId;
    private SwipeType type;
}
