package soulfit.soulfit.meeting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soulfit.soulfit.meeting.client.AiMeetingClient;
import soulfit.soulfit.meeting.domain.MeetingReview;
import soulfit.soulfit.meeting.dto.ai.AiReviewRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiReviewResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiReviewAnalysisServiceTest {

    @Mock
    private AiMeetingClient aiMeetingClient;

    @InjectMocks
    private AiReviewAnalysisService aiReviewAnalysisService;

    @BeforeEach
    void setUp() {
        // No WebClient.Builder mocking needed for RestTemplate client
        // @InjectMocks will automatically inject aiMeetingClient into aiReviewAnalysisService
    }

    @Test
    void analyzeReviewsByRestTemplate_shouldSendCorrectRequestAndReturnSummary() {
        // Given
        MeetingReview review1 = new MeetingReview();
        review1.setContent("시간 가는 줄 모르고 대화했네요. 정말 즐거웠어요!");
        review1.setMeetingRating(5.0);
        review1.setCreatedAt(LocalDateTime.of(2025, 10, 1, 12, 0, 0));

        MeetingReview review2 = new MeetingReview();
        review2.setContent("대화가 잘 통해서 좋았습니다.");
        review2.setMeetingRating(4.0);
        review2.setCreatedAt(LocalDateTime.of(2025, 10, 1, 12, 5, 0));

        List<MeetingReview> meetingReviews = Arrays.asList(review1, review2);

        AiReviewResponseDto mockResponse = new AiReviewResponseDto("사용자들은 대화가 잘 통하고 즐거웠다는 긍정적인 피드백을 남겼습니다.");
        when(aiMeetingClient.analyzeMeetingReview(any(AiReviewRequestDto.class))).thenReturn(mockResponse);

        // When
        AiReviewResponseDto actualResponse = aiReviewAnalysisService.analyzeReviewsByRestTemplate(meetingReviews);

        // Then
        assertEquals(mockResponse.getSummary(), actualResponse.getSummary());

        // Verify AiMeetingClient call details
        ArgumentCaptor<AiReviewRequestDto> requestDtoCaptor = ArgumentCaptor.forClass(AiReviewRequestDto.class);
        verify(aiMeetingClient).analyzeMeetingReview(requestDtoCaptor.capture());

        AiReviewRequestDto capturedRequestDto = requestDtoCaptor.getValue();
        assertEquals(2, capturedRequestDto.getReviews().size());

        AiReviewRequestDto.ReviewData capturedReview1 = capturedRequestDto.getReviews().get(0);
        assertEquals("시간 가는 줄 모르고 대화했네요. 정말 즐거웠어요!", capturedReview1.getContent());
        assertEquals(5, capturedReview1.getRating());
        assertEquals("2025-10-01T12:00:00Z", capturedReview1.getCreatedAt());

        AiReviewRequestDto.ReviewData capturedReview2 = capturedRequestDto.getReviews().get(1);
        assertEquals("대화가 잘 통해서 좋았습니다.", capturedReview2.getContent());
        assertEquals(4, capturedReview2.getRating());
        assertEquals("2025-10-01T12:05:00Z", capturedReview2.getCreatedAt());
    }
}