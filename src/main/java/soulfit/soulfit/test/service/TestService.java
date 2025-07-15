package soulfit.soulfit.test.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.test.domain.*;
import soulfit.soulfit.test.dto.*;
import soulfit.soulfit.test.repository.ChoiceRepository;
import soulfit.soulfit.test.repository.TestAnswerRepository;
import soulfit.soulfit.test.repository.TestQuestionRepository;
import soulfit.soulfit.test.repository.TestSessionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestQuestionRepository testQuestionRepository;
    private final ChoiceRepository choiceRepository;
    private final TestSessionRepository testSessionRepository;
    private final TestAnswerRepository testAnswerRepository;

    /**
     * 검사 시작: 세션 생성 + 질문 목록 반환
     */
    @Transactional
    public StartTestSessionResponse startTest(TestType testType, UserAuth user) {
        // 1. 세션 생성
        TestSession session = new TestSession();
        session.setUser(user);
        session.setTestType(testType);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(SessionStatus.IN_PROGRESS);

        testSessionRepository.save(session);

        // 2. 질문 + 선택지 조회
        List<TestQuestion> questions = testQuestionRepository.findByTestType(testType);

        List<QuestionResponse> questionDtos = questions.stream().map(q -> {
            if (q.getType() == ValueQuestionType.MULTIPLE) {
                List<Choice> choices = choiceRepository.findByQuestionId(q.getId());
                return new QuestionResponse(q, choices);
            } else {
                return new QuestionResponse(q);
            }
        }).collect(Collectors.toList());

        return new StartTestSessionResponse(session.getId(), questionDtos);
    }

    /**
     * 응답 제출
     */
    @Transactional
    public void submitAnswers(SubmitAnswerRequest dto, UserAuth user) {
        TestSession session = testSessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("세션 없음"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한 없음");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("이미 제출된 검사");
        }

        List<TestAnswer> answers = new ArrayList<>();
        for (SubmitAnswerRequest.AnswerDto a : dto.getAnswers()) {
            TestAnswer answer = new TestAnswer();
            answer.setSession(session);

            TestQuestion question = new TestQuestion();
            question.setId(a.getQuestionId());
            answer.setQuestion(question); // 성능을 위해 proxy 참조

            if (a.getSelectedChoiceId() != null) {
                Choice choice = new Choice();
                choice.setId(a.getSelectedChoiceId());
                answer.setSelectedChoice(choice);
            }

            answer.setAnswerText(a.getAnswerText());
            answers.add(answer);
        }

        testAnswerRepository.saveAll(answers);

        session.setSubmittedAt(LocalDateTime.now());
        session.setStatus(SessionStatus.COMPLETED);
        testSessionRepository.save(session);
    }

    @Transactional
    public UserTestResult getUserTestResult(UserAuth user, TestType testType) {
        // 1. 완료된 세션 찾기 (가장 최근 것 기준)
        TestSession session = testSessionRepository.findFirstByUserIdAndTestTypeAndStatusOrderBySubmittedAtDesc(
                        user.getId(), testType, SessionStatus.COMPLETED)
                .orElseThrow(() -> new IllegalArgumentException("해당 검사 결과가 존재하지 않습니다."));

        // 2. 세션에 포함된 응답 불러오기
        List<TestAnswer> answers = testAnswerRepository.findBySessionId(session.getId());

        // 3. 응답 DTO 변환
        List<TestAnswerResponse> answerDtos = answers.stream().map(answer -> {
            TestQuestion q = answer.getQuestion();

            String selectedChoiceText = null;
            if (q.getType() == ValueQuestionType.MULTIPLE && answer.getSelectedChoice() != null) {
                selectedChoiceText = answer.getSelectedChoice().getText();
            }

            return new TestAnswerResponse(
                    q.getId(),
                    q.getContent(),
                    q.getType(),
                    selectedChoiceText,
                    answer.getAnswerText()
            );
        }).collect(Collectors.toList());

        return new UserTestResult(
                session.getId(),
                session.getTestType(),
                session.getSubmittedAt(),
                answerDtos
        );
    }

}
