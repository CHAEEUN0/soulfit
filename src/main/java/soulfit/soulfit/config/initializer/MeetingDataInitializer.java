package soulfit.soulfit.config.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingQuestionRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!test")
@Order(2) // Ensure this runs after UserInitializer
public class MeetingDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingQuestionRepository meetingQuestionRepository;

    @Autowired
    private MeetingParticipantRepository meetingParticipantRepository;

    @Override
    public void run(String... args) throws Exception {
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

            MeetingQuestion question1 = MeetingQuestion.create("운동 경력은 어떻게 되시나요?");
            question1.setMeeting(sampleMeeting);


            meetingQuestionRepository.saveAll(List.of(question1));

            // Add 'user' as a participant to the sample meeting
            UserAuth regularUser = userRepository.findByUsername("user").orElseThrow();
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeeting(sampleMeeting);
            participant.setUser(regularUser);
            participant.setApprovalStatus(ApprovalStatus.PENDING);
            participant.setJoinedAt(LocalDateTime.now());
            meetingParticipantRepository.save(participant);

            System.out.println("Sample meeting and questions created.");
        }
    }
}
