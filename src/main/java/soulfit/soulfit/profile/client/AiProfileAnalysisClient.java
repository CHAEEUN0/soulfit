
package soulfit.soulfit.profile.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.profile.dto.ai.ProfileAnalysisRequestDto;
import soulfit.soulfit.profile.dto.ai.ProfileAnalysisResponseDto;


@Component
@RequiredArgsConstructor
public class AiProfileAnalysisClient {

    private final RestTemplate restTemplate;

    //TODO : 진짜 IP 변경
    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    public ProfileAnalysisResponseDto detectFakeProfile(ProfileAnalysisRequestDto requestDto) {
        return restTemplate.postForObject(
                aiServerUrl + "/detect-fake-profile",
                requestDto,
                ProfileAnalysisResponseDto.class);
    }
}