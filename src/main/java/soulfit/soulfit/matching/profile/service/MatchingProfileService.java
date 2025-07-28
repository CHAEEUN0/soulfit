package soulfit.soulfit.matching.profile.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.profile.domain.IdealTypeKeyword;
import soulfit.soulfit.matching.profile.domain.MatchingProfile;
import soulfit.soulfit.matching.profile.dto.MatchingProfileRequestDto;
import soulfit.soulfit.matching.profile.dto.MatchingProfileResponseDto;
import soulfit.soulfit.matching.profile.repository.IdealTypeKeywordRepository;
import soulfit.soulfit.matching.profile.repository.MatchingProfileRepository;

@Service
@RequiredArgsConstructor
public class MatchingProfileService {

    private final MatchingProfileRepository matchingProfileRepository;
    private final IdealTypeKeywordRepository idealTypeKeywordRepository;

    // 자신의 매칭 프로필 수정
    @Transactional
    public MatchingProfileResponseDto updateMyProfile(UserAuth user, MatchingProfileRequestDto dto) {
        MatchingProfile profile = matchingProfileRepository.findByUserAuthId(user.getId())
                .orElseThrow(() -> new IllegalStateException("매칭 프로필이 존재하지 않습니다."));

        profile.setBio(dto.bio());
        profile.setJob(dto.job());
        profile.setHeightCm(dto.heightCm());
        profile.setWeightKg(dto.weightKg());
        profile.setReligion(dto.religion());
        profile.setSmoking(dto.smoking());
        profile.setDrinking(dto.drinking());
        profile.setVisibility(dto.visibility());

        profile.getIdealTypes().clear();
        dto.idealTypes().forEach(keyword -> {
            IdealTypeKeyword tag = idealTypeKeywordRepository.findByKeyword(keyword)
                    .orElseGet(() -> idealTypeKeywordRepository.save(new IdealTypeKeyword(null, keyword)));
            profile.getIdealTypes().add(tag);
        });

        return MatchingProfileResponseDto.from(profile);
    }

    // 타인의 매칭 프로필 조회
    @Transactional(readOnly = true)
    public MatchingProfileResponseDto getProfileByUserId(Long userId) {
        MatchingProfile profile = matchingProfileRepository.findByUserAuthId(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저의 매칭 프로필이 존재하지 않습니다."));
        return MatchingProfileResponseDto.from(profile);
    }
}
