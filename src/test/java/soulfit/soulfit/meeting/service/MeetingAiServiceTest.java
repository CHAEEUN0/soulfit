package soulfit.soulfit.meeting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.client.AiMeetingClient;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.dto.MeetingResponseDto;
import soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantResponse;
import soulfit.soulfit.meeting.dto.ai.AiRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiResponseDto;
import soulfit.soulfit.meeting.repository.MeetingBookmarkRepository;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.profile.domain.UserProfile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingAiServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MeetingAiServiceTest.class);

    @InjectMocks
    private MeetingService meetingService;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;

    @Mock
    private MeetingBookmarkRepository meetingBookmarkRepository;

    @Mock
    private AiMeetingClient aiMeetingClient;

    private UserAuth user;
    private Meeting meeting1, meeting2, meeting3;

    @BeforeEach
    void setUp() {
        user = new UserAuth("testuser", "password", "test@test.com");
        ReflectionTestUtils.setField(user, "id", 1L);

        Keyword keyword1 = new Keyword();
        ReflectionTestUtils.setField(keyword1, "name", "Java");
        Keyword keyword2 = new Keyword();
        ReflectionTestUtils.setField(keyword2, "name", "Spring");

        meeting1 = Meeting.builder().id(1L).title("Java Study").category(Category.STUDY).keywords(Set.of(keyword1)).build();
        meeting2 = Meeting.builder().id(2L).title("Spring Project").category(Category.STUDY).keywords(Set.of(keyword2)).build();
        meeting3 = Meeting.builder().id(3L).title("Running Crew").category(Category.WORKOUT).bookmarkCount(10).build();
    }

    @Test
    @DisplayName("AI 추천 성공 시 추천된 미팅 목록과 추천 이유를 반환한다")
    void getRecommendedMeetings_Success() {
        // given
        Page<Meeting> participatedMeetings = new PageImpl<>(List.of(meeting1));
        Page<MeetingBookmark> bookmarkedMeetings = new PageImpl<>(List.of(new MeetingBookmark(meeting2, user)));

        AiResponseDto.RecommendationItem item1 = new AiResponseDto.RecommendationItem();
        ReflectionTestUtils.setField(item1, "meetingId", 1L);
        ReflectionTestUtils.setField(item1, "reasonKeywords", List.of("#참여_기반", "#스터디"));

        AiResponseDto.RecommendationItem item2 = new AiResponseDto.RecommendationItem();
        ReflectionTestUtils.setField(item2, "meetingId", 2L);
        ReflectionTestUtils.setField(item2, "reasonKeywords", List.of("#북마크_기반", "#프로젝트"));

        AiResponseDto aiResponse = new AiResponseDto();
        ReflectionTestUtils.setField(aiResponse, "recommendations", List.of(item1, item2));

        when(meetingParticipantRepository.findMeetingUserParticipated(any(UserAuth.class), any(PageRequest.class)))
                .thenReturn(participatedMeetings);
        when(meetingBookmarkRepository.findByUserOrderByCreatedAtDesc(any(UserAuth.class), any(PageRequest.class)))
                .thenReturn(bookmarkedMeetings);
        when(aiMeetingClient.getRecommendations(any(AiRequestDto.class))).thenReturn(aiResponse);
        when(meetingRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(meeting1, meeting2));

        // when
        List<MeetingResponseDto> result = meetingService.getRecommendedMeetings(user);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getRecommendationReasons()).containsExactly("#참여_기반", "#스터디");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getRecommendationReasons()).containsExactly("#북마크_기반", "#프로젝트");

        ArgumentCaptor<AiRequestDto> captor = ArgumentCaptor.forClass(AiRequestDto.class);
        verify(aiMeetingClient, times(1)).getRecommendations(captor.capture());
        AiRequestDto capturedRequest = captor.getValue();
        log.info("==== AI Request DTO ====");
        log.info("UserId: {}", capturedRequest.getUserId());
        log.info("Recent Categories: {}", capturedRequest.getRecentCategories());
        log.info("Recent Keywords: {}", capturedRequest.getRecentKeywords());
        log.info("Bookmarked Categories: {}", capturedRequest.getBookmarkedCategories());
        log.info("========================");

        log.info("==== Recommended Meetings with Reasons ====");
        result.forEach(r -> log.info("Meeting ID: {}, Title: {}, Reasons: {}", r.getId(), r.getTitle(), r.getRecommendationReasons()));
        log.info("=========================================");
    }

    @Test
    @DisplayName("AI 서버 실패 시 대체 로직으로 인기순 미팅 목록을 반환한다")
    void getRecommendedMeetings_AiClientFails_ReturnsFallback() {
        // given
        when(meetingParticipantRepository.findMeetingUserParticipated(any(UserAuth.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(meetingBookmarkRepository.findByUserOrderByCreatedAtDesc(any(UserAuth.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(aiMeetingClient.getRecommendations(any(AiRequestDto.class))).thenThrow(new RuntimeException("AI server is down"));
        
        Page<Meeting> fallbackMeetings = new PageImpl<>(List.of(meeting3));
        when(meetingRepository.findAll(any(PageRequest.class))).thenReturn(fallbackMeetings);

        // when
        List<MeetingResponseDto> result = meetingService.getRecommendedMeetings(user);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
        verify(meetingRepository, times(1)).findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bookmarkCount")));

        log.info("==== Fallback Meetings ====");
        result.forEach(r -> log.info("Meeting ID: {}, Title: {}", r.getId(), r.getTitle()));
        log.info("=========================");
    }

    @Test
    @DisplayName("AI 서버가 빈 목록 반환 시 빈 리스트를 반환한다")
    void getRecommendedMeetings_AiClientReturnsEmpty_ReturnsEmptyList() {
        // given
        AiResponseDto emptyResponse = new AiResponseDto();
        ReflectionTestUtils.setField(emptyResponse, "recommendations", Collections.emptyList());

        when(meetingParticipantRepository.findMeetingUserParticipated(any(UserAuth.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(meetingBookmarkRepository.findByUserOrderByCreatedAtDesc(any(UserAuth.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(aiMeetingClient.getRecommendations(any(AiRequestDto.class))).thenReturn(emptyResponse);

        // when
        List<MeetingResponseDto> result = meetingService.getRecommendedMeetings(user);

        // then
        assertThat(result).isEmpty();
        verify(meetingRepository, never()).findAllById(any());

        log.info("==== AI Returned Empty List ====");
        log.info("Final result size: {}", result.size());
        log.info("==============================");
    }

    @Test
    @DisplayName("참가자 분석 요청 시 승인된 참가자의 성별과 연령대를 정확히 집계하여 AI 클라이언트를 호출한다")
    void analyzeParticipants_Success() {
        // given
        MeetingAiService meetingAiService = new MeetingAiService(meetingRepository, aiMeetingClient);
        Long meetingId = 1L;

        // --- 테스트용 참가자 데이터 설정 ---
        // 20대 남성 1, 30대 남성 1, 20대 여성 2, 승인 대기 1
        UserAuth user1 = new UserAuth(); ReflectionTestUtils.setField(user1, "id", 101L);
        UserProfile profile1 = new UserProfile(user1, LocalDate.now().minusYears(25), soulfit.soulfit.profile.domain.Gender.MALE);
        user1.setUserProfile(profile1);

        UserAuth user2 = new UserAuth(); ReflectionTestUtils.setField(user2, "id", 102L);
        UserProfile profile2 = new UserProfile(user2, LocalDate.now().minusYears(32), soulfit.soulfit.profile.domain.Gender.MALE);
        user2.setUserProfile(profile2);

        UserAuth user3 = new UserAuth(); ReflectionTestUtils.setField(user3, "id", 103L);
        UserProfile profile3 = new UserProfile(user3, LocalDate.now().minusYears(28), soulfit.soulfit.profile.domain.Gender.FEMALE);
        user3.setUserProfile(profile3);

        UserAuth user4 = new UserAuth(); ReflectionTestUtils.setField(user4, "id", 104L);
        UserProfile profile4 = new UserProfile(user4, LocalDate.now().minusYears(29), soulfit.soulfit.profile.domain.Gender.FEMALE);
        user4.setUserProfile(profile4);

        UserAuth user5 = new UserAuth(); ReflectionTestUtils.setField(user5, "id", 105L);
        UserProfile profile5 = new UserProfile(user5, LocalDate.now().minusYears(35), soulfit.soulfit.profile.domain.Gender.FEMALE);
        user5.setUserProfile(profile5);

        MeetingParticipant mp1 = new MeetingParticipant(); mp1.setUser(user1); mp1.setApprovalStatus(soulfit.soulfit.meeting.domain.ApprovalStatus.APPROVED);
        MeetingParticipant mp2 = new MeetingParticipant(); mp2.setUser(user2); mp2.setApprovalStatus(soulfit.soulfit.meeting.domain.ApprovalStatus.APPROVED);
        MeetingParticipant mp3 = new MeetingParticipant(); mp3.setUser(user3); mp3.setApprovalStatus(soulfit.soulfit.meeting.domain.ApprovalStatus.APPROVED);
        MeetingParticipant mp4 = new MeetingParticipant(); mp4.setUser(user4); mp4.setApprovalStatus(soulfit.soulfit.meeting.domain.ApprovalStatus.APPROVED);
        MeetingParticipant mp5 = new MeetingParticipant(); mp5.setUser(user5); mp5.setApprovalStatus(soulfit.soulfit.meeting.domain.ApprovalStatus.PENDING);

        meeting1.getMeetingParticipants().clear(); // 이전 테스트의 영향을 없애기 위해 초기화
        meeting1.getMeetingParticipants().addAll(List.of(mp1, mp2, mp3, mp4, mp5));

        // --- Mock 객체 행동 정의 ---
        AiAnalyzeParticipantResponse mockResponse = new AiAnalyzeParticipantResponse();
        ReflectionTestUtils.setField(mockResponse, "message", "Analysis successful");

        when(meetingRepository.findById(meetingId)).thenReturn(java.util.Optional.of(meeting1));
        when(aiMeetingClient.analyzeParticipants(any(soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantRequest.class))).thenReturn(mockResponse);

        // when
        AiAnalyzeParticipantResponse response = meetingAiService.analyzeParticipants(meetingId);

        // then
        // 1. Mock 객체 호출 검증
        ArgumentCaptor<soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantRequest> captor = ArgumentCaptor.forClass(soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantRequest.class);
        verify(aiMeetingClient, times(1)).analyzeParticipants(captor.capture());

        // 2. 반환된 결과 검증
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Analysis successful");

        // 3. AI 클라이언트로 전달된 데이터 검증
        soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getMeetingId()).isEqualTo(meetingId);
        assertThat(capturedRequest.getGenderCounts())
                .hasSize(2)
                .containsEntry("MALE", 2)
                .containsEntry("FEMALE", 2);
        assertThat(capturedRequest.getAgeBandCounts())
                .hasSize(2)
                .containsEntry("TWENTIES", 3)
                .containsEntry("THIRTIES", 1);

        log.info("==== Captured AI Analyze Request DTO ====");
        log.info("MeetingId: {}", capturedRequest.getMeetingId());
        log.info("Gender Counts: {}", capturedRequest.getGenderCounts());
        log.info("AgeBand Counts: {}", capturedRequest.getAgeBandCounts());
        log.info("=========================================");
    }


}
