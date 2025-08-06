package soulfit.soulfit.valuestest.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.valuestest.dto.ai.ValuesTestAnalysisRequestDto;
import soulfit.soulfit.valuestest.dto.ai.ValuesTestAnalysisResponseDto;

@Component
@RequiredArgsConstructor
public class AiValuesTestRestTemplateClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public ValuesTestAnalysisResponseDto analyzeValues(ValuesTestAnalysisRequestDto requestDto) {
        String url = aiServerUrl + "/analyze-values";
        // AI 서버에 POST 요청을 보내고 응답을 DTO로 받음
        return restTemplate.postForObject(url, requestDto, ValuesTestAnalysisResponseDto.class);
    }
}
