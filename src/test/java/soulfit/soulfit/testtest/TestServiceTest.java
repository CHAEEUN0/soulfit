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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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
        // 테스트용 사용자 저장
        testUser = new UserAuth();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser.setEnabled(true);
        userAuthRepository.save(testUser);

        // 테스트 질문 및 선택지 세팅
        TestQuestion q1 = new TestQuestion();
        q1.setTestType(TestType.TYPE_A);
        q1.setContent("당신은 아침형 인간입니까?");
        q1.setType(ValueQuestionType.MULTIPLE);
        testQuestionRepository.save(q1);

        choiceRepository.saveAll(List.of(
                createChoice(q1, "예"),
                createChoice(q1, "아니오")
        ));

        TestQuestion q2 = new TestQuestion();
        q2.setTestType(TestType.TYPE_A);
        q2.setContent("자신을 한 문장으로 소개해주세요.");
        q2.setType(ValueQuestionType.TEXT);
        testQuestionRepository.save(q2);
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
        assertThat(storedAnswers.size()).isEqualTo(17);

    }
}
