package soulfit.soulfit.matching.profile.dto;

import soulfit.soulfit.matching.profile.domain.DrinkingHabit;
import soulfit.soulfit.matching.profile.domain.Religion;
import soulfit.soulfit.matching.profile.domain.SmokingHabit;
import soulfit.soulfit.matching.profile.domain.Visibility;

import java.util.Set;

// MatchingProfileRequestDto.java
public record MatchingProfileRequestDto(
        String bio,
        String job,
        Integer heightCm,
        Integer weightKg,
        Religion religion,
        SmokingHabit smoking,
        DrinkingHabit drinking,
        Visibility visibility,
        Set<String> idealTypes
) {}

