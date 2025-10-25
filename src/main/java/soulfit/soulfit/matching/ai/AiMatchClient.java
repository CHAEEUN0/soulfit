package soulfit.soulfit.matching.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.matching.ai.dto.AiMatchResultDto;

import java.util.Arrays;

@Component
@Slf4j
public class AiMatchClient {
    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Autowired
    public AiMatchClient(@Qualifier("aiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiMatchResponseDto match(AiMatchRequestDto requestDto) {
        AiMatchResultDto[] results = restTemplate.postForObject(
                aiServerUrl + "/matching/match-users",
                requestDto,
                AiMatchResultDto[].class
        );
        if (results == null) {
            return new AiMatchResponseDto();
        }
        return new AiMatchResponseDto(Arrays.asList(results));
    }
}
