package soulfit.soulfit.matching.profile.dto;

import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.profile.domain.*;
import soulfit.soulfit.profile.domain.MbtiType;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.domain.PersonalityKeyword;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// MatchingProfileResponseDto.java
public record MatchingProfileResponseDto(
        Long id,
        String bio,
        String job,
        Integer heightCm,
        Integer weightKg,
        Religion religion,
        SmokingHabit smoking,
        DrinkingHabit drinking,
        Visibility visibility,
        Set<String> idealTypes,
        Long userId,
        String username,
        Integer age,
        MbtiType mbti,
        String profileImageUrl,
        List<String> personalityKeywords
) {
    public static MatchingProfileResponseDto from(MatchingProfile profile) {
        UserAuth user = profile.getUserAuth();
        UserProfile userProfile = user.getUserProfile();

        return new MatchingProfileResponseDto(
                profile.getId(),
                profile.getBio(),
                profile.getJob(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getReligion(),
                profile.getSmoking(),
                profile.getDrinking(),
                profile.getVisibility(),
                profile.getIdealTypes().stream()
                        .map(IdealTypeKeyword::getKeyword)
                        .collect(Collectors.toSet()),
                user.getId(),
                user.getUsername(),
                calculateAge(userProfile.getBirthDate()),
                userProfile.getMbti(),
                userProfile.getProfileImageUrl(),
                userProfile.getPersonalityKeywords().stream()
                        .map(PersonalityKeyword::getKeyword)
                        .collect(Collectors.toList())
        );
    }

    private static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}

