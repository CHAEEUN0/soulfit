package soulfit.soulfit.matching.profile;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.profile.domain.*;
import soulfit.soulfit.matching.profile.dto.MatchingProfileRequestDto;
import soulfit.soulfit.matching.profile.dto.MatchingProfileResponseDto;
import soulfit.soulfit.matching.profile.repository.IdealTypeKeywordRepository;
import soulfit.soulfit.matching.profile.repository.MatchingProfileRepository;
import soulfit.soulfit.matching.profile.service.MatchingProfileService;
import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.MbtiType;
import soulfit.soulfit.profile.domain.UserProfile;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingProfileServiceTest {

    @InjectMocks
    private MatchingProfileService matchingProfileService;

    @Mock
    private MatchingProfileRepository matchingProfileRepository;

    @Mock
    private IdealTypeKeywordRepository idealTypeKeywordRepository;

    private UserAuth user;
    private MatchingProfile profile;
    private MatchingProfileRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new UserAuth("testuser", "password123", "test@test.com");
        user.setId(1L);

        UserProfile userProfile = new UserProfile(user, LocalDate.now().minusYears(25), Gender.MALE);
        userProfile.setMbti(MbtiType.INFP);
        user.setUserProfile(userProfile);

        profile = MatchingProfile.builder()
                .id(1L)
                .userAuth(user)
                .bio("기존 자기소개")
                .job("개발자")
                .heightCm(180)
                .weightKg(75)
                .religion(Religion.NONE)
                .smoking(SmokingHabit.NON_SMOKER)
                .drinking(DrinkingHabit.SOMETIMES)
                .visibility(Visibility.PUBLIC)
                .idealTypes(new HashSet<>())
                .build();

        Set<String> idealTypes = Set.of("운동", "독서");
        requestDto = new MatchingProfileRequestDto(
                "새로운 자기소개",      // bio
                "디자이너",          // job
                175,                 // heightCm
                70,                  // weightKg
                Religion.CHRISTIANITY, // religion
                SmokingHabit.REGULAR,  // smoking
                DrinkingHabit.OFTEN,   // drinking
                Visibility.PUBLIC,   // visibility
                idealTypes           // idealTypes
        );
    }

    @Test
    @DisplayName("자신의 매칭 프로필 수정 성공")
    void updateMyProfile_success() {
        // given
        given(matchingProfileRepository.findByUserAuthId(user.getId())).willReturn(Optional.of(profile));
        given(idealTypeKeywordRepository.findByKeyword("운동")).willReturn(Optional.of(new IdealTypeKeyword(1L, "운동")));
        given(idealTypeKeywordRepository.findByKeyword("독서")).willReturn(Optional.empty());
        given(idealTypeKeywordRepository.save(any(IdealTypeKeyword.class))).willReturn(new IdealTypeKeyword(2L, "독서"));

        // when
        MatchingProfileResponseDto responseDto = matchingProfileService.updateMyProfile(user, requestDto);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.bio()).isEqualTo(requestDto.bio());
        assertThat(responseDto.job()).isEqualTo(requestDto.job());
        assertThat(responseDto.heightCm()).isEqualTo(requestDto.heightCm());
        assertThat(responseDto.weightKg()).isEqualTo(requestDto.weightKg());
        assertThat(responseDto.religion()).isEqualTo(requestDto.religion());
        assertThat(responseDto.smoking()).isEqualTo(requestDto.smoking());
        assertThat(responseDto.drinking()).isEqualTo(requestDto.drinking());
        assertThat(responseDto.visibility()).isEqualTo(requestDto.visibility());
        assertThat(responseDto.idealTypes()).containsExactlyInAnyOrder("운동", "독서");

        verify(matchingProfileRepository, times(1)).findByUserAuthId(user.getId());
        verify(idealTypeKeywordRepository, times(1)).findByKeyword("운동");
        verify(idealTypeKeywordRepository, times(1)).findByKeyword("독서");
        verify(idealTypeKeywordRepository, times(1)).save(any(IdealTypeKeyword.class));
    }

    @Test
    @DisplayName("타인의 매칭 프로필 조회 성공")
    void getProfileByUserId_success() {
        // given
        given(matchingProfileRepository.findByUserAuthId(user.getId())).willReturn(Optional.of(profile));

        // when
        MatchingProfileResponseDto responseDto = matchingProfileService.getProfileByUserId(user.getId());

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isEqualTo(profile.getId());
        assertThat(responseDto.bio()).isEqualTo(profile.getBio());
        assertThat(responseDto.username()).isEqualTo(user.getUsername());
        assertThat(responseDto.age()).isEqualTo(25);
        assertThat(responseDto.mbti()).isEqualTo(MbtiType.INFP);
        assertThat(responseDto.profileImageUrl()).isNull(); // 초기값이 null일 수 있음
        assertThat(responseDto.personalityKeywords()).isEmpty(); // 초기값이 없음

        verify(matchingProfileRepository, times(1)).findByUserAuthId(user.getId());
    }

    @Test
    @DisplayName("타인의 매칭 프로필 조회 실패 - 프로필 없음")
    void getProfileByUserId_fail_whenProfileNotFound() {
        // given
        given(matchingProfileRepository.findByUserAuthId(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchingProfileService.getProfileByUserId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 유저의 매칭 프로필이 존재하지 않습니다.");

        verify(matchingProfileRepository, times(1)).findByUserAuthId(anyLong());
    }
}