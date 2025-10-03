package soulfit.soulfit.meeting.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.meeting.dto.ai.*;

@Component
public class AiMeetingClient {

    private final RestTemplate restTemplate;

    //TODO : 진짜 IP 변경
    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Autowired
    public AiMeetingClient(@Qualifier("aiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiResponseDto getRecommendations(AiRequestDto requestDto) {
        return restTemplate.postForObject(aiServerUrl + "/recommend", requestDto, AiResponseDto.class);
    }

    public AiAnalyzeParticipantResponse analyzeParticipants(AiAnalyzeParticipantRequest requestDto) {
        return restTemplate.postForObject(aiServerUrl + "/analyze-participants", requestDto, AiAnalyzeParticipantResponse.class);
    }

    public AiReviewResponseDto analyzeMeetingReview(AiReviewRequestDto requestDto) {
        return restTemplate.postForObject(
                aiServerUrl + "/meeting/analyze-reviews",
                requestDto,
                AiReviewResponseDto.class);
    }
}
