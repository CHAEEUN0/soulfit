
package soulfit.soulfit.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.community.post.PostRepository;
import soulfit.soulfit.profile.client.AiProfileAnalysisClient;
import soulfit.soulfit.profile.domain.ProfileAnalysisReport;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.dto.ai.ProfileAnalysisRequestDto;
import soulfit.soulfit.profile.dto.ai.ProfileAnalysisResponseDto;
import soulfit.soulfit.profile.repository.ProfileAnalysisReportRepository;
import soulfit.soulfit.profile.service.ProfileAnalysisService;
import soulfit.soulfit.report.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProfileAnalysisServiceTest {

    @InjectMocks
    private ProfileAnalysisService profileAnalysisService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AiProfileAnalysisClient aiProfileAnalysisClient;

    @Mock
    private ProfileAnalysisReportRepository profileAnalysisReportRepository;

    private UserAuth testUser;
    private UserProfile testUserProfile;

    @BeforeEach
    void setUp() {
        testUser = new UserAuth("testUser", "password", "test@test.com");
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(10));
        testUser.setAccountStatus(AccountStatus.ACTIVE);

        testUserProfile = new UserProfile();
        testUserProfile.setBio("Test introduction");
        testUserProfile.setProfileImageUrl("http://test.com/profile.jpg");
        testUser.setUserProfile(testUserProfile);
    }

    @Test
    @DisplayName("AI 분석 결과, 허위 프로필로 판단되어 사용자 상태가 UNDER_REVIEW로 변경된다")
    void analyzeAndProcessProfile_FakeProfile_StatusChanges() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(postRepository.countByPoster(testUser)).willReturn(0);
        given(reportRepository.countByTargetId(testUser.getId())).willReturn(5); // 피신고 횟수가 많음

        ProfileAnalysisResponseDto fakeResponse = new ProfileAnalysisResponseDto();
        fakeResponse.setUserId(1L);
        fakeResponse.setFake(true);
        fakeResponse.setFakeScore(0.9);
        fakeResponse.setReasons(Collections.singletonList("HIGH_REPORT_COUNT"));

        given(aiProfileAnalysisClient.detectFakeProfile(any(ProfileAnalysisRequestDto.class))).willReturn(fakeResponse);

        // when
        profileAnalysisService.analyzeAndProcessProfile(1L);

        // then
        assertThat(testUser.getAccountStatus()).isEqualTo(AccountStatus.UNDER_REVIEW);
        verify(userRepository).save(testUser);
        verify(profileAnalysisReportRepository).save(any(ProfileAnalysisReport.class));
    }

    @Test
    @DisplayName("AI 분석 결과, 정상 프로필로 판단되어 사용자 상태가 변경되지 않는다")
    void analyzeAndProcessProfile_NormalProfile_StatusUnchanged() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(postRepository.countByPoster(testUser)).willReturn(10);
        given(reportRepository.countByTargetId(testUser.getId())).willReturn(0);

        ProfileAnalysisResponseDto normalResponse = new ProfileAnalysisResponseDto();
        normalResponse.setUserId(1L);
        normalResponse.setFake(false);
        normalResponse.setFakeScore(0.1);
        normalResponse.setReasons(Collections.emptyList());

        given(aiProfileAnalysisClient.detectFakeProfile(any(ProfileAnalysisRequestDto.class))).willReturn(normalResponse);

        // when
        profileAnalysisService.analyzeAndProcessProfile(1L);

        // then
        assertThat(testUser.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(profileAnalysisReportRepository).save(any(ProfileAnalysisReport.class));
    }

    @Test
    @DisplayName("AI 서버 통신 실패 시, 사용자 상태가 변경되지 않고 에러 로그가 기록된다")
    void analyzeAndProcessProfile_AiServerError_StatusUnchanged() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(aiProfileAnalysisClient.detectFakeProfile(any(ProfileAnalysisRequestDto.class))).willThrow(new RuntimeException("AI server is down"));

        // when
        profileAnalysisService.analyzeAndProcessProfile(1L);

        // then
        assertThat(testUser.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }
}
