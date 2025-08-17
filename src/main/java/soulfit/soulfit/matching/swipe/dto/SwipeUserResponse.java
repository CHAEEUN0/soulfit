package soulfit.soulfit.matching.swipe.dto;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.authentication.entity.UserAuth;

@Getter
@Builder
public class SwipeUserResponse {
    private Long id;
    private String username;
    private String profileImageUrl;

    public static SwipeUserResponse from(UserAuth user) {
        String imageUrl = (user.getUserProfile() != null) ? user.getUserProfile().getProfileImageUrl() : null;
        return SwipeUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImageUrl(imageUrl)
                .build();
    }
}
