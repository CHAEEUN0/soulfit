package soulfit.soulfit.chat.ai.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.chat.ai.*;

import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class AiChatClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Autowired
    public AiChatClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AIAnalysisResponseDto analyzeChat(List<String> messages) {
        // ================= 템플릿 리턴 로직 시작 =================
        log.warn("AI Server is not ready. Returning mock response.");

        // Random 객체를 사용하여 매번 다른 분석 결과를 반환하도록 설정
        Random random = new Random();
        double positiveScore = random.nextDouble() * 0.6 + 0.2; // 0.2 ~ 0.8
        double negativeScore = random.nextDouble() * 0.2;       // 0.0 ~ 0.2

        List<String> moods = List.of("UPBEAT", "CALM", "TENSE", "EXCITED");
        String randomMood = moods.get(random.nextInt(moods.size()));

        AIAnalysisResponseDto mockResponse = new AIAnalysisResponseDto();
        mockResponse.setPositiveScore(positiveScore);
        mockResponse.setNegativeScore(negativeScore);
        mockResponse.setMood(randomMood);
        mockResponse.setKeywords(List.of("#임시", "#키워드", "#테스트"));

        return mockResponse;

        /* ================= 템플릿 리턴 로직 종료 =================

        // ================= 실제 로직 시작 =================
//        try {
//            return restTemplate.postForObject(
//                    aiServerUrl + "/analyze",
//                    messages,
//                    AIAnalysisResponseDto.class
//            );
//        } catch (RestClientException e) {
//            log.error("AI Server request failed for chat analysis. Error: {}", e.getMessage());
//            return null;
//        }
        // ================= 실제 로직 종료 ================= */
    }


    public AiChatAnalysisResponseDto analyzeMessage(AiChatAnalysisRequestDto requestDto) {
        return restTemplate.postForObject(
                aiServerUrl + "/Chat/",
                requestDto,
                AiChatAnalysisResponseDto.class);
       }


    public AiRecommendResponseDto recommendChat(AiRecommendRequestDto requestDto){
        return restTemplate.postForObject(
                aiServerUrl + "/Chat/recommend",
                requestDto,
                AiRecommendResponseDto.class);
    }
}
