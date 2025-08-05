package soulfit.soulfit.testtest;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.valuestest.domain.*;
import soulfit.soulfit.valuestest.dto.QuestionResponse;
import soulfit.soulfit.valuestest.dto.StartTestSessionResponse;
import soulfit.soulfit.valuestest.dto.SubmitAnswerRequest;
import soulfit.soulfit.valuestest.repository.ChoiceRepository;
import soulfit.soulfit.valuestest.repository.TestAnswerRepository;
import soulfit.soulfit.valuestest.repository.TestQuestionRepository;
import soulfit.soulfit.valuestest.repository.TestSessionRepository;
import soulfit.soulfit.valuestest.service.TestService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"ai.server.url=http://localhost:8081"})
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TestServiceTest {

    @Autowired
    private TestService testService;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private TestSessionRepository testSessionRepository;

    @Autowired
    private TestAnswerRepository testAnswerRepository;

    @Autowired
    private UserRepository userAuthRepository;

    private UserAuth testUser;

    @BeforeEach
    void setUp() {
        // Clear repositories to ensure test isolation
        testAnswerRepository.deleteAllInBatch();
        testSessionRepository.deleteAllInBatch();
        choiceRepository.deleteAllInBatch();
        testQuestionRepository.deleteAllInBatch();
        userAuthRepository.deleteAllInBatch();

        // Setup test user
        testUser = new UserAuth();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser.setEnabled(true);
        userAuthRepository.save(testUser);

        // Setup 15 test questions and choices to ensure deterministic test data
        List<TestQuestion> questions = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            TestQuestion q = new TestQuestion();
            q.setTestType(TestType.TYPE_A);
            q.setContent("Test Question " + i);
            // Create 10 multiple choice and 5 text questions
            q.setType(i <= 10 ? ValueQuestionType.MULTIPLE : ValueQuestionType.TEXT);
            questions.add(q);
        }
        testQuestionRepository.saveAll(questions);

        List<Choice> choices = new ArrayList<>();
        for (TestQuestion q : questions) {
            if (q.getType() == ValueQuestionType.MULTIPLE) {
                choices.add(createChoice(q, "Yes"));
                choices.add(createChoice(q, "No"));
            }
        }
        choiceRepository.saveAll(choices);
    }

    private Choice createChoice(TestQuestion question, String text) {
        Choice choice = new Choice();
        choice.setQuestion(question);
        choice.setText(text);
        return choice;
    }

    @Test
    void testStartAndSubmitTestFlow() {
        // Step 1: 테스트 시작
        StartTestSessionResponse response = testService.startTest(TestType.TYPE_A, testUser);

        Long sessionId = response.getSessionId();

        // Step 2: 응답 준비
        List<SubmitAnswerRequest.AnswerDto> answers = new ArrayList<>();
        for (QuestionResponse q : response.getQuestions()) {
            SubmitAnswerRequest.AnswerDto a = new SubmitAnswerRequest.AnswerDto();
            a.setQuestionId(q.getId());

            if (q.getType() == ValueQuestionType.MULTIPLE) {
                System.out.println("malsdfasdfj");
                a.setSelectedChoiceId(q.getChoices().get(0).getId()); // 첫 번째 보기 선택
            } else if (q.getType() == ValueQuestionType.TEXT) {
                a.setAnswerText("나는 긍정적인 사람입니다.");
            }

            answers.add(a);
        }

        SubmitAnswerRequest submitDto = new SubmitAnswerRequest();
        submitDto.setSessionId(sessionId);
        submitDto.setAnswers(answers);

        // Step 3: 응답 제출
        testService.submitAnswers(submitDto, testUser);

        // Step 4: 상태 확인
        TestSession completedSession = testSessionRepository.findById(sessionId).orElseThrow();
        assertThat(completedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(completedSession.getSubmittedAt()).isNotNull();

        List<TestAnswer> storedAnswers = testAnswerRepository.findBySessionId(sessionId);
        assertThat(storedAnswers.size()).isEqualTo(15);

    }
}
