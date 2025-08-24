
package soulfit.soulfit.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import soulfit.soulfit.profile.domain.MbtiType;
import soulfit.soulfit.profile.domain.PersonalityKeyword;
import soulfit.soulfit.profile.domain.UserProfile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String nickname;
    private LocalDate birthDate;
    private String gender;
    private MbtiType mbti;
    private String profileImageUrl;
    private String bio;
    private List<String> personalityKeywords;
    private String region;
    private Double latitude;
    private Double longitude;

    public static UserProfileResponse from(UserProfile userProfile) {
        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .nickname(userProfile.getUserAuth().getUsername()) // UserAuth에서 닉네임 가져오기
                .birthDate(userProfile.getBirthDate())
                .gender(userProfile.getGender().name())
                .mbti(userProfile.getMbti())
                .profileImageUrl(userProfile.getProfileImageUrl())
                .bio(userProfile.getBio())
                .personalityKeywords(userProfile.getPersonalityKeywords().stream()
                        .map(PersonalityKeyword::getKeyword)
                        .collect(Collectors.toList()))
                .region(userProfile.getRegion())
                .latitude(userProfile.getLatitude())
                .longitude(userProfile.getLongitude())
                .build();
    }
}
