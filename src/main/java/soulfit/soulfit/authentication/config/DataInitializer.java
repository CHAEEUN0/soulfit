package soulfit.soulfit.authentication.config;


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

import java.time.LocalDateTime;
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
                    .location(new Location("city","road","zip",0.999,0.111))
                    .fee(10000)
                    .meetingTime(LocalDateTime.now().plusDays(7))
                    .recruitDeadline(LocalDateTime.now().plusDays(3))
                    .maxParticipants(10)
                    .currentParticipants(0)
                    .status(MeetingStatus.OPEN)
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
            participant.setApproval_status(Approvalstatus.PENDING);
            participant.setJoined_at(LocalDateTime.now());
            meetingParticipantRepository.save(participant);

            System.out.println("Sample meeting and questions created.");
        }
    }
}