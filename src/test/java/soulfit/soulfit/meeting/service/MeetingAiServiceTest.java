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
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Keyword;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingBookmark;
import soulfit.soulfit.meeting.dto.MeetingResponseDto;
import soulfit.soulfit.meeting.dto.ai.AiRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiResponseDto;
import soulfit.soulfit.meeting.repository.MeetingBookmarkRepository;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;

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
}
