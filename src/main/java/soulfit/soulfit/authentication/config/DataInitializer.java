package soulfit.soulfit.authentication.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.test.domain.Choice;
import soulfit.soulfit.test.domain.QuestionType;
import soulfit.soulfit.test.domain.TestQuestion;
import soulfit.soulfit.test.domain.TestType;
import soulfit.soulfit.test.repository.ChoiceRepository;
import soulfit.soulfit.test.repository.TestQuestionRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            UserAuth admin = new UserAuth();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setAccountStatus(AccountStatus.ACTIVE);
            userRepository.save(admin);

            System.out.println("Admin user created: username=admin, password=admin123");
        }

        // Create regular user if not exists
        if (!userRepository.existsByUsername("user")) {
            UserAuth userAuth = new UserAuth();
            userAuth.setUsername("user");
            userAuth.setPassword(passwordEncoder.encode("user123"));
            userAuth.setEmail("user@example.com");
            userAuth.setRole(Role.USER);
            userAuth.setEnabled(true);
            userAuth.setAccountStatus(AccountStatus.ACTIVE);
            userRepository.save(userAuth);

            System.out.println("Regular user created: username=user, password=user123");
        }

        initTestQuestionsFromJson();
    }

    private void initTestQuestionsFromJson() throws IOException {
        if (testQuestionRepository.count() > 0) return;

        loadTestQuestions("TestType_A.json", TestType.TYPE_A);
        loadTestQuestions("TestType_B.json", TestType.TYPE_B);

        System.out.println("✅ Test questions loaded from JSON");
    }

    private void loadTestQuestions(String fileName, TestType testType) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/" + fileName);

        System.out.println("testType = " + testType);

        if (is == null) {
            System.err.println("⚠️ JSON file not found: " + fileName);
            return;
        }

        List<QuestionJsonDto> questions = Arrays.asList(
                objectMapper.readValue(is, QuestionJsonDto[].class)
        );

        for (QuestionJsonDto dto : questions) {
            QuestionType questionType = QuestionType.valueOf(dto.getType().toUpperCase());

            TestQuestion question = new TestQuestion();
            question.setTestType(testType);
            question.setContent(dto.getContent());
            question.setType(questionType);
            testQuestionRepository.save(question);

            if (questionType == QuestionType.MULTIPLE && dto.getChoices() != null) {
                List<Choice> choices = dto.getChoices().stream()
                        .map(text -> {
                            Choice c = new Choice();
                            c.setQuestion(question);
                            c.setText(text);
                            return c;
                        }).toList();
                choiceRepository.saveAll(choices);
            }
        }
    }

    @Data
    public static class QuestionJsonDto {
        private String content;
        private String type;
        private List<String> choices; // 선택형 질문일 경우에만 존재
    }
}