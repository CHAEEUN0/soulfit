package soulfit.soulfit.testtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.valuestest.domain.*;
import soulfit.soulfit.valuestest.dto.StartTestSessionResponse;
import soulfit.soulfit.valuestest.dto.SubmitAnswerRequest;
import soulfit.soulfit.valuestest.dto.UserTestResult;
import soulfit.soulfit.valuestest.repository.ChoiceRepository;
import soulfit.soulfit.valuestest.repository.TestQuestionRepository;
import soulfit.soulfit.valuestest.repository.TestSessionRepository;
import soulfit.soulfit.valuestest.repository.ValuesTestAnalysisReportRepository;
import soulfit.soulfit.valuestest.service.TestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(properties = {"ai.server.url=http://localhost:8081"})
@Transactional
@ActiveProfiles("test")

public class ValuesTestAnalysisIntegrationTest {

    @Autowired
    private TestService testService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestSessionRepository testSessionRepository;

    @Autowired
    private ValuesTestAnalysisReportRepository reportRepository;

    @Autowired
    @Qualifier("aiRestTemplate") // Use the AI-specific RestTemplate
    private RestTemplate aiRestTemplate; // Renamed for clarity

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    private final Logger log = LoggerFactory.getLogger(ValuesTestAnalysisIntegrationTest.class);

    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserAuth testUser;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(aiRestTemplate);


        // 테스트 유저 생성 및 저장
        testUser = new UserAuth();
        testUser.setEmail("testuser@test.com");
        testUser.setPassword("password");
        testUser.setUsername("test_user");
        testUser.setRole(Role.USER);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("답변 제출 시 AI 분석이 호출되고 결과가 성공적으로 저장된다")
    void submitAnswers_triggersAIAnalysis_andSavesReport() throws Exception {
                log.info("--- Test: {} ---", "답변 제출 시 AI 분석이 호출되고 결과가 성공적으로 저장된다");
        // Given: 테스트용 질문과 선택지 생성
        log.info("Step 1: Creating test question and choice...");
        TestQuestion question = new TestQuestion();
        question.setTestType(TestType.TYPE_A);
        question.setContent("Test Question 1");
        question.setType(ValueQuestionType.MULTIPLE);
        testQuestionRepository.save(question);
        log.info("Saved question with ID: {}", question.getId());

        Choice choice = new Choice();
        choice.setQuestion(question);
        choice.setText("Test Choice 1");
        choiceRepository.save(choice);
        log.info("Saved choice with ID: {}", choice.getId());

        // 검사 시작 및 답변 준비
        log.info("Step 2: Starting test session...");
        StartTestSessionResponse sessionResponse = testService.startTest(TestType.TYPE_A, testUser);
        Long sessionId = sessionResponse.getSessionId();
        log.info("Test session started with ID: {}", sessionId);

        SubmitAnswerRequest.AnswerDto answerDto = new SubmitAnswerRequest.AnswerDto();
        answerDto.setQuestionId(question.getId());
        answerDto.setSelectedChoiceId(choice.getId());

        SubmitAnswerRequest submitRequest = new SubmitAnswerRequest();
        submitRequest.setSessionId(sessionId);
        submitRequest.setAnswers(Collections.singletonList(answerDto));
        log.info("Step 3: Prepared answer submission request for session ID: {}", sessionId);

        // AI 서버의 예상 응답 설정
        String expectedResponseJson = objectMapper.writeValueAsString(Map.of(
                "analysis_summary", "This is a test summary.",
                "top_values", List.of("Honesty", "Creativity")
        ));
        log.info("Step 4: Mocking AI server response.");

        // Mocking: AI 서버에 대한 POST 요청을 가로채고, 위에서 정의한 응답을 반환하도록 설정
        mockServer.expect(requestTo("http://localhost:8081/analyze-values"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(expectedResponseJson, MediaType.APPLICATION_JSON));

        // When: 답변 제출
        log.info("Step 5: Submitting answers...");
        try {
            testService.submitAnswers(submitRequest, testUser);
            log.info("Answer submission completed without exceptions.");
        } catch (Exception e) {
            log.error("Exception occurred during submitAnswers: {}", e.getMessage(), e);
            fail("Exception was thrown during submitAnswers: " + e.getMessage());
        }

        // Then: AI 분석 결과가 DB에 저장되었는지 확인
        log.info("Step 6: Verifying results...");
        mockServer.verify(); // Mock 서버에 대한 호출이 있었는지 검증
        log.info("Mock server verification successful.");

        TestSession completedSession = testSessionRepository.findById(sessionId).orElseThrow();
        assertEquals(SessionStatus.COMPLETED, completedSession.getStatus());

        ValuesTestAnalysisReport savedReport = reportRepository.findByTestSessionId(sessionId).orElseThrow();
        assertNotNull(savedReport);
        assertEquals("This is a test summary.", savedReport.getAnalysisSummary());
        assertTrue(savedReport.getTopValues().contains("Honesty"));
    }

    @Test
    @DisplayName("AI 분석 완료 후 결과 조회 시 분석 내용이 포함된다")
    void getUserTestResult_includesAIAnalysis() throws Exception {
        log.info("--- Test: {} ---", "AI 분석 완료 후 결과 조회 시 분석 내용이 포함된다");
        // Given: 테스트용 질문과 선택지 생성
        log.info("Step 1: Creating test question and choice...");
        TestQuestion question = new TestQuestion();
        question.setTestType(TestType.TYPE_A);
        question.setContent("Test Question 2");
        question.setType(ValueQuestionType.MULTIPLE);
        testQuestionRepository.save(question);
        log.info("Saved question with ID: {}", question.getId());

        Choice choice = new Choice();
        choice.setQuestion(question);
        choice.setText("Test Choice 2");
        choiceRepository.save(choice);
        log.info("Saved choice with ID: {}", choice.getId());

        // 답변 제출 및 AI 분석 완료
        log.info("Step 2: Starting test session...");
        StartTestSessionResponse sessionResponse = testService.startTest(TestType.TYPE_A, testUser);
        Long sessionId = sessionResponse.getSessionId();
        log.info("Test session started with ID: {}", sessionId);

        SubmitAnswerRequest.AnswerDto answerDto = new SubmitAnswerRequest.AnswerDto();
        answerDto.setQuestionId(question.getId());
        answerDto.setSelectedChoiceId(choice.getId());

        SubmitAnswerRequest submitRequest = new SubmitAnswerRequest();
        submitRequest.setSessionId(sessionId);
        submitRequest.setAnswers(Collections.singletonList(answerDto));
        log.info("Step 3: Prepared answer submission request for session ID: {}", sessionId);

        String expectedResponseJson = objectMapper.writeValueAsString(Map.of(
                "analysis_summary", "AI-powered analysis result.",
                "top_values", List.of("Growth", "Collaboration")
        ));
        log.info("Step 4: Mocking AI server response.");


        mockServer.expect(requestTo("http://localhost:8081/analyze-values"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(expectedResponseJson, MediaType.APPLICATION_JSON));

        log.info("Step 5: Submitting answers...");
        try {
            testService.submitAnswers(submitRequest, testUser);
            log.info("Answer submission completed without exceptions.");
        } catch (Exception e) {
            log.error("Exception occurred during submitAnswers: {}", e.getMessage(), e);
            fail("Exception was thrown during submitAnswers: " + e.getMessage());
        }

        // When: 결과 조회
        log.info("Step 6: Getting user test result...");
        UserTestResult result = testService.getUserTestResult(testUser, TestType.TYPE_A);
        log.info("Successfully retrieved user test result.");

        // Then: 조회 결과에 AI 분석 내용이 포함되어 있는지 확인
        log.info("Step 7: Verifying results...");
        mockServer.verify();
        log.info("Mock server verification successful.");
        assertNotNull(result);
        assertNotNull(result.getAiResult());
        assertEquals("AI-powered analysis result.", result.getAiResult().getSummary());
        assertEquals(2, result.getAiResult().getTopValues().size());
        assertEquals("Growth", result.getAiResult().getTopValues().get(0));
    }
}
