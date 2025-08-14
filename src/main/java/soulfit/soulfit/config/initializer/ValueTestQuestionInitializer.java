package soulfit.soulfit.config.initializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.valuestest.domain.Choice;
import soulfit.soulfit.valuestest.domain.TestQuestion;
import soulfit.soulfit.valuestest.domain.TestType;
import soulfit.soulfit.valuestest.domain.ValueQuestionType;
import soulfit.soulfit.valuestest.repository.ChoiceRepository;
import soulfit.soulfit.valuestest.repository.TestQuestionRepository;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@Order(3) // Runs after other initializers, order not strictly critical but good for consistency
public class ValueTestQuestionInitializer implements CommandLineRunner {

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
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
            ValueQuestionType questionType = ValueQuestionType.valueOf(dto.getType().toUpperCase());

            TestQuestion question = new TestQuestion();
            question.setTestType(testType);
            question.setContent(dto.getContent());
            question.setType(questionType);
            testQuestionRepository.save(question);

            if (questionType == ValueQuestionType.MULTIPLE && dto.getChoices() != null) {
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
