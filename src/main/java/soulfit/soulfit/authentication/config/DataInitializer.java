package soulfit.soulfit.authentication.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingQuestionRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.valuestest.domain.Choice;
import soulfit.soulfit.valuestest.domain.TestQuestion;
import soulfit.soulfit.valuestest.domain.TestType;
import soulfit.soulfit.valuestest.domain.ValueQuestionType;
import soulfit.soulfit.valuestest.repository.ChoiceRepository;
import soulfit.soulfit.valuestest.repository.TestQuestionRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingQuestionRepository meetingQuestionRepository;

    @Autowired
    private MeetingParticipantRepository meetingParticipantRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

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

        // Create sample meeting data
        if (meetingRepository.count() == 0) {
            UserAuth adminUser = userRepository.findByUsername("admin").orElseThrow();

            Meeting sampleMeeting = Meeting.builder()
                    .title("Sample Fitness Meeting")
                    .description("A meeting for fitness enthusiasts.")
                    .host(adminUser)
                    .category(Category.HOBBY)
                    .location(new Location("city","district","address","detailAddress", "zip",0.999,0.111))
                    .fee(10000)
                    .meetingTime(LocalDateTime.now().plusDays(7))
                    .recruitDeadline(LocalDateTime.now().plusDays(3))
                    .maxParticipants(10)
                    .currentParticipants(0)
                    .meetingStatus(MeetingStatus.OPEN)
                    .build();
            meetingRepository.save(sampleMeeting);

            MeetingQuestion question1 = MeetingQuestion.createMeetingQuestion("운동 경력은 어떻게 되시나요?", QuestionType.TEXT, 1, null);
            question1.setMeeting(sampleMeeting);
            MeetingQuestion question2 = MeetingQuestion.createMeetingQuestion("선호하는 운동 종류는?", QuestionType.MULTIPLE_CHOICE, 2, List.of("요가", "필라테스", "헬스", "크로스핏"));
            question2.setMeeting(sampleMeeting);

            meetingQuestionRepository.saveAll(List.of(question1, question2));

            // Add 'user' as a participant to the sample meeting
            UserAuth regularUser = userRepository.findByUsername("user").orElseThrow();
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeeting(sampleMeeting);
            participant.setUser(regularUser);
            participant.setApprovalStatus(ApprovalStatus.PENDING);
            participant.setJoinedAt(LocalDateTime.now());
            meetingParticipantRepository.save(participant);

            System.out.println("Sample meeting and questions created.");

            initTestQuestionsFromJson();
        }
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