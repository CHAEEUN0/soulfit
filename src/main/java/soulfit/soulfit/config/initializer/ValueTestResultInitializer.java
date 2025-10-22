package soulfit.soulfit.config.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.valuestest.domain.*;
import soulfit.soulfit.valuestest.repository.*;
import soulfit.soulfit.valuestest.service.ValuesTestAnalysisService;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@Profile("!test")
@Order(4) // Runs after UserInitializer (Order 1) and ValueTestQuestionInitializer (Order 3)
@RequiredArgsConstructor
public class ValueTestResultInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final ChoiceRepository choiceRepository;
    private final TestSessionRepository testSessionRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final ValuesTestAnalysisService valuesTestAnalysisService;

    @Override
    public void run(String... args) throws Exception {
        if (testSessionRepository.count() > 0) {
            System.out.println("Value test sessions already exist. Skipping initialization.");
            return;
        }

        List<String> usernames = List.of("user", "user2", "user3", "user4");
        Random random = new Random();

        for (String username : usernames) {
            Optional<UserAuth> userOptional = userRepository.findByUsername(username);
            userOptional.ifPresent(user -> {
                try {
                    // Simulate TYPE_A test completion
                    simulateTestCompletion(user, TestType.TYPE_A, random);
                    // Simulate TYPE_B test completion
                    simulateTestCompletion(user, TestType.TYPE_B, random);
                } catch (Exception e) {
                    System.err.println("Error simulating test completion for user " + username + ": " + e.getMessage());
                }
            });
        }
        System.out.println("✅ Sample value test results initialized for users: " + usernames);
    }

    private void simulateTestCompletion(UserAuth user, TestType testType, Random random) {
        // Create a new test session
        TestSession session = new TestSession();
        session.setUser(user);
        session.setTestType(testType);
        session.setStartedAt(LocalDateTime.now().minusMinutes(30)); // Started 30 mins ago
        session.setSubmittedAt(LocalDateTime.now());
        session.setStatus(SessionStatus.COMPLETED);
        testSessionRepository.save(session);

        // Get all questions for the test type
        List<TestQuestion> questions = testQuestionRepository.findByTestType(testType);

        for (TestQuestion question : questions) {
            TestAnswer answer = new TestAnswer();
            answer.setSession(session);
            answer.setQuestion(question);

            if (question.getType() == ValueQuestionType.MULTIPLE) {
                // Select a random choice for multiple-choice questions
                List<Choice> choices = choiceRepository.findByQuestionId(question.getId());
                if (!choices.isEmpty()) {
                    Choice selectedChoice = choices.get(random.nextInt(choices.size()));
                    answer.setSelectedChoice(selectedChoice);
                }
            } else {
                // For other types, just put some dummy text
                answer.setAnswerText("Sample answer for " + question.getContent());
            }
            testAnswerRepository.save(answer);
        }

        // Trigger AI analysis and report saving
        valuesTestAnalysisService.analyzeAndSaveReport(session);
    }
}
