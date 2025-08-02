package soulfit.soulfit.meeting.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.meeting.dto.ai.AiRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiResponseDto;

@Component
@RequiredArgsConstructor
public class AiMeetingClient {

    private final RestTemplate restTemplate;

    //TODO : 진짜 IP 변경
    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    public AiResponseDto getRecommendations(AiRequestDto requestDto) {
        return restTemplate.postForObject(aiServerUrl + "/recommend", requestDto, AiResponseDto.class);
    }
}
