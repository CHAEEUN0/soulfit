package soulfit.soulfit.matching.swipe.dto;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.matching.profile.domain.MatchingProfile;

@Getter
@Builder
public class SwipeTargetUserResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private int age;
    private double distanceInKm;
    private Integer height;
    private String smokingStatus;
    private String drinkingStatus;

    public static SwipeTargetUserResponse from(UserAuth userAuth, UserProfile userProfile, MatchingProfile matchingProfile, double distanceInKm) {
        return SwipeTargetUserResponse.builder()
                .userId(userAuth.getId())
                .nickname(userAuth.getUsername())
                .profileImageUrl(userProfile != null ? userProfile.getProfileImageUrl() : null)
                .age(userProfile != null ? java.time.Period.between(userProfile.getBirthDate(), java.time.LocalDate.now()).getYears() : 0)
                .distanceInKm(distanceInKm)
                .height(matchingProfile != null ? matchingProfile.getHeightCm() : null)
                .smokingStatus(matchingProfile != null && matchingProfile.getSmoking() != null ? matchingProfile.getSmoking().name() : null)
                .drinkingStatus(matchingProfile != null && matchingProfile.getDrinking() != null ? matchingProfile.getDrinking().name() : null)
                .build();
    }
}
