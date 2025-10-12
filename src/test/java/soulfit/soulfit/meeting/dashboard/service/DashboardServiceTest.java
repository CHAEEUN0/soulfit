
package soulfit.soulfit.meeting.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.dashboard.dto.MeetingStatsResponseDto;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingReviewRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;

    @Mock
    private MeetingReviewRepository meetingReviewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private UserAuth testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserAuth();
        testUser.setId(1L);
    }

    @Test
    @DisplayName("모임 활동 통계 계산 성공")
    void calculateMeetingStats_success() {
        // Arrange
        Location location1 = Location.builder().city("서울").build();
        Location location2 = Location.builder().city("부산").build();

        List<Meeting> mockMeetings = Arrays.asList(
                Meeting.builder().meetingTime(LocalDateTime.of(2025, 9, 5, 19, 0)) // 금, 저녁
                        .category(Category.WORKOUT).maxParticipants(10).location(location1).build(),
                Meeting.builder().meetingTime(LocalDateTime.of(2025, 9, 12, 20, 0)) // 금, 저녁 (시간 변경)
                        .category(Category.WORKOUT).maxParticipants(5).location(location1).build(),
                Meeting.builder().meetingTime(LocalDateTime.of(2025, 8, 1, 10, 0)) // 월, 오전
                        .category(Category.STUDY).maxParticipants(20).location(location2).build()
        );

        List<MeetingReview> mockReviews = Arrays.asList(
                MeetingReview.builder().meetingRating(5.0).build(),
                MeetingReview.builder().meetingRating(4.0).build()
        );

        double mockAvgHostRating = 4.5;

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(meetingParticipantRepository.findAllMeetingUserParticipated(testUser)).thenReturn(mockMeetings);
        when(meetingReviewRepository.findAllByUser(testUser)).thenReturn(mockReviews);
        when(meetingReviewRepository.findAverageHostRatingByHostId(testUser.getId())).thenReturn(mockAvgHostRating);

        // Act
        MeetingStatsResponseDto stats = dashboardService.calculateMeetingStats(testUser.getId());

        // Assert
        assertThat(stats).isNotNull();

        // 기본 통계 검증
        assertThat(stats.getTotalMeetingsAttended()).isEqualTo(3);
        assertThat(stats.getMonthlyActivity()).containsEntry("2025-09", 2L).containsEntry("2025-08", 1L);
        assertThat(stats.getCategoryDistribution()).containsEntry("WORKOUT", 2L).containsEntry("STUDY", 1L);
        assertThat(stats.getMostAttendedCategory()).isEqualTo("WORKOUT");

        // 활동 패턴 분석 검증
        assertThat(stats.getFavoriteDayOfWeek()).isEqualTo("금요일");
        assertThat(stats.getFavoriteTimeOfDay()).isEqualTo("저녁");

        // 모임 선호도 분석 검증
        assertThat(stats.getAverageMeetingSize()).isEqualTo((10 + 5 + 20) / 3.0);
        assertThat(stats.getMostFrequentRegion()).isEqualTo("서울");

        // 리뷰 기반 분석 검증
        assertThat(stats.getAverageRatingGiven()).isEqualTo(4.5);
        assertThat(stats.getAverageRatingReceivedAsHost()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("신규 사용자 - 활동 데이터가 없는 경우")
    void calculateMeetingStats_newUser_noActivity() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(meetingParticipantRepository.findAllMeetingUserParticipated(testUser)).thenReturn(Collections.emptyList());
        when(meetingReviewRepository.findAllByUser(testUser)).thenReturn(Collections.emptyList());
        when(meetingReviewRepository.findAverageHostRatingByHostId(testUser.getId())).thenReturn(null);

        // Act
        MeetingStatsResponseDto stats = dashboardService.calculateMeetingStats(testUser.getId());

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalMeetingsAttended()).isZero();
        assertThat(stats.getMonthlyActivity()).isEmpty();
        assertThat(stats.getCategoryDistribution()).isEmpty();
        assertThat(stats.getMostAttendedCategory()).isNull();
        assertThat(stats.getFavoriteDayOfWeek()).isNull();
        assertThat(stats.getFavoriteTimeOfDay()).isNull();
        assertThat(stats.getAverageMeetingSize()).isZero();
        assertThat(stats.getMostFrequentRegion()).isNull();
        assertThat(stats.getAverageRatingGiven()).isZero();
        assertThat(stats.getAverageRatingReceivedAsHost()).isZero();
    }

    @Test
    @DisplayName("부분 데이터 - 리뷰 활동이 없는 경우")
    void calculateMeetingStats_partialActivity_noReviews() {
        // Arrange
        Location location1 = Location.builder().city("서울").build();
        List<Meeting> mockMeetings = Collections.singletonList(
                Meeting.builder().meetingTime(LocalDateTime.of(2025, 9, 5, 19, 0))
                        .category(Category.WORKOUT).maxParticipants(10).location(location1).build()
        );

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(meetingParticipantRepository.findAllMeetingUserParticipated(testUser)).thenReturn(mockMeetings);
        when(meetingReviewRepository.findAllByUser(testUser)).thenReturn(Collections.emptyList());
        when(meetingReviewRepository.findAverageHostRatingByHostId(testUser.getId())).thenReturn(null);

        // Act
        MeetingStatsResponseDto stats = dashboardService.calculateMeetingStats(testUser.getId());

        // Assert
        assertThat(stats).isNotNull();
        // 모임 관련 통계는 정상 계산
        assertThat(stats.getTotalMeetingsAttended()).isEqualTo(1);
        assertThat(stats.getMostAttendedCategory()).isEqualTo("WORKOUT");
        // 리뷰 관련 통계는 0으로 처리
        assertThat(stats.getAverageRatingGiven()).isZero();
        assertThat(stats.getAverageRatingReceivedAsHost()).isZero();
    }

    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 예외 발생")
    void calculateMeetingStats_userNotFound_throwsException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dashboardService.calculateMeetingStats(999L);
        });
    }
}
