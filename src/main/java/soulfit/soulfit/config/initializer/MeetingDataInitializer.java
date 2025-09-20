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
import java.util.ArrayList;
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
        // Create sample meeting data for each category
        if (meetingRepository.count() == 0) {
            UserAuth adminUser = userRepository.findByUsername("admin").orElseThrow();
            List<Meeting> meetingsToSave = new ArrayList<>();

            List<Location> locations = List.of(
                new Location("서울특별시", "강남구", "테헤란로 231", "601호", "06142", 37.5045, 127.0489),
                new Location("부산광역시", "해운대구", "해운대해변로 266", "101동 502호", "48096", 35.1587, 129.1604),
                new Location("제주특별자치도", "제주시", "첨단로 242", "", "63309", 33.4507, 126.5707),
                new Location("대구광역시", "중구", "동성로2길 81", "3층", "41911", 35.8693, 128.5933),
                new Location("인천광역시", "연수구", "컨벤시아대로 165", "210호", "21998", 37.3943, 126.6399),
                new Location("광주광역시", "서구", "상무중앙로 7", "8층", "61945", 35.1525, 126.8514),
                new Location("대전광역시", "유성구", "대학로 99", "W2-1 301호", "34141", 36.3742, 127.3658)
            );

            Category[] categories = Category.values();
            for (int i = 0; i < categories.length; i++) {
                Category category = categories[i];
                // Cycle through locations if there are more categories than locations
                Location location = locations.get(i % locations.size());

                LocalDateTime meetingTime = LocalDateTime.now().plusDays(7 + i * 2);
                LocalDateTime recruitDeadline = meetingTime.minusDays(3);

                Meeting meeting = Meeting.builder()
                        .title("Sample " + category + " Meeting")
                        .description("A meeting for " + category.toString().toLowerCase() + " enthusiasts in " + location.getCity() + ".")
                        .host(adminUser)
                        .category(category)
                        .location(location)
                        .fee(10000 + (i * 1000)) // 10000, 11000, 12000...
                        .meetingTime(meetingTime)
                        .recruitDeadline(recruitDeadline)
                        .maxParticipants(10)
                        .currentParticipants(0)
                        .meetingStatus(MeetingStatus.OPEN)
                        .build();
                meetingsToSave.add(meeting);
            }

            List<Meeting> savedMeetings = meetingRepository.saveAll(meetingsToSave);

            if (!savedMeetings.isEmpty()) {
                // Get the first meeting from the saved list to add question and participant
                Meeting firstMeeting = savedMeetings.get(0);

                MeetingQuestion question1 = MeetingQuestion.create("이 모임에 참여하고 싶은 이유를 알려주세요.");
                question1.setMeeting(firstMeeting);
                meetingQuestionRepository.save(question1);

                // Add 'user' as a participant to the first sample meeting
                UserAuth regularUser = userRepository.findByUsername("user").orElseThrow();
                MeetingParticipant participant = new MeetingParticipant();
                participant.setMeeting(firstMeeting);
                participant.setUser(regularUser);
                participant.setApprovalStatus(ApprovalStatus.PENDING);
                participant.setJoinedAt(LocalDateTime.now());
                meetingParticipantRepository.save(participant);
            }

            System.out.println("Sample meetings for all categories created.");
        }
    }
}
