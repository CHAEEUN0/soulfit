package soulfit.soulfit.matching.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
        return restTemplate.postForObject(
                aiServerUrl+"/matching/match-users",
                requestDto,
                AiMatchResponseDto.class
        );
    }
}

