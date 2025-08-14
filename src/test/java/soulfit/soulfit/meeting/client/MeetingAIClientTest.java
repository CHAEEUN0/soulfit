package soulfit.soulfit.meeting.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantRequest;
import soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantResponse;
import soulfit.soulfit.core.config.RestTemplateConfig;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource(properties = "ai.server.url=http://mock-ai-server:8000")
class MeetingAIClientTest {

    @Autowired
    private AiMeetingClient aiMeetingClient;

    @Autowired
    @Qualifier("aiRestTemplate")
    private RestTemplate aiRestTemplate;

    private MockRestServiceServer mockServer;


    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(aiRestTemplate).build();
        mockServer.reset();
    }

    @Test
    @DisplayName("참가자 분석 요청 시 snake_case 형식의 JSON으로 올바른 요청을 보낸다")
    void analyzeParticipants_sendsCorrectRequest_inSnakeCase() throws JsonProcessingException {
        // given: 테스트에 사용할 요청 데이터 준비
        AiAnalyzeParticipantRequest requestDto = AiAnalyzeParticipantRequest.builder()
                .meetingId(101L)
                .genderCounts(Map.of("MALE", 3, "FEMALE", 3))
                .ageBandCounts(Map.of("TWENTIES", 4, "THIRTIES", 2))
                .build();

        // given: 예상되는 요청 본문 (snake_case JSON)
        String expectedRequestBody = objectMapper.writeValueAsString(requestDto);

        // given: AI 서버로부터 받을 예상 응답
        String mockResponse = "{\"message\" : \"msg\"}";

        // expect: Mock 서버가 어떤 요청을 받을지 설정
        mockServer.expect(requestTo(aiServerUrl + "/analyze-participants"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedRequestBody))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when: 실제 테스트 대상 메소드 호출
        AiAnalyzeParticipantResponse response = aiMeetingClient.analyzeParticipants(requestDto);

        // then: Mock 서버가 예상한 요청을 모두 받았는지 검증
        mockServer.verify();
        // then: 반환된 응답 객체가 null이 아닌지 확인
        assertThat(response).isNotNull();
    }
}