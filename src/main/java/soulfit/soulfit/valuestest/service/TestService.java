package soulfit.soulfit.valuestest.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.valuestest.domain.*;
import soulfit.soulfit.valuestest.dto.*;
import soulfit.soulfit.valuestest.repository.ChoiceRepository;
import soulfit.soulfit.valuestest.repository.TestAnswerRepository;
import soulfit.soulfit.valuestest.repository.TestQuestionRepository;
import soulfit.soulfit.valuestest.repository.TestSessionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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

            List<ChoiceResponse> choiceDtos = null;
            Long selectedChoiceId = null;

            if (q.getType() == ValueQuestionType.MULTIPLE) {
                // 질문에 해당하는 모든 선택지 조회 (DB의 PK 순서대로 정렬된다고 가정)
                List<Choice> choices = choiceRepository.findByQuestionId(q.getId());

                // choiceId를 1부터 시작하는 상대적인 값으로 변환
                final AtomicLong counter = new AtomicLong(1);
                choiceDtos = choices.stream()
                        .map(c -> new ChoiceResponse(counter.getAndIncrement(), c.getText()))
                        .collect(Collectors.toList());

                // 사용자가 선택한 choice의 상대적인 ID를 찾음
//                if (answer.getSelectedChoice() != null) {
//                    long dbSelectedId = answer.getSelectedChoice().getId();
//                    for (int i = 0; i < choices.size(); i++) {
//                        if (choices.get(i).getId().equals(dbSelectedId)) {
//                            selectedChoiceId = (long) i + 1;
//                            break;
//                        }
//                    }
//                }

                selectedChoiceId = answer.getSelectedChoice().getId();
            }

            return new TestAnswerResponse(
                    q.getId(),
                    q.getContent(),
                    q.getType(),
                    choiceDtos, // 전체 선택지 목록
                    selectedChoiceId, // 선택된 선택지 ID
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
