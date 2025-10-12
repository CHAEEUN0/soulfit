package soulfit.soulfit.config.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!test")
@Order(2) // Run after UserInitializer
@RequiredArgsConstructor
public class MeetingDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    @Override
    public void run(String... args) throws Exception {
        if (meetingRepository.count() > 0) {
            return; // Data already initialized
        }

        // Get users
        UserAuth user = userRepository.findByUsername("user").orElseThrow();
        UserAuth user2 = userRepository.findByUsername("user2").orElseThrow();
        UserAuth user3 = userRepository.findByUsername("user3").orElseThrow();

        // === Create Sample Meetings ===

        // Meeting 1: Hosted by user2, attended by user (last month, WORKOUT, Seoul)
        Meeting meeting1 = meetingRepository.save(Meeting.builder()
                .title("저녁 런닝 크루")
                .description("같이 달려요!")
                .host(user2)
                .category(Category.WORKOUT)
                .location(new Location("서울", "강남구", "테헤란로", "", "", 0, 0))
                .meetingTime(LocalDateTime.now().minusMonths(1).withHour(19))
                .recruitDeadline(LocalDateTime.now().minusMonths(1).minusDays(1))
                .maxParticipants(10)
                .meetingStatus(MeetingStatus.FINISHED)
                .build());

        // Meeting 2: Hosted by user3, attended by user (last month, WORKOUT, Seoul)
        Meeting meeting2 = meetingRepository.save(Meeting.builder()
                .title("주말 코딩 스터디")
                .description("알고리즘 문제 풀이")
                .host(user3)
                .category(Category.STUDY)
                .location(new Location("서울", "마포구", "양화로", "", "", 0, 0))
                .meetingTime(LocalDateTime.now().minusMonths(1).withHour(14))
                .recruitDeadline(LocalDateTime.now().minusMonths(1).minusDays(2))
                .maxParticipants(5)
                .meetingStatus(MeetingStatus.FINISHED)
                .build());

        // Meeting 3: Hosted by user3, attended by user (2 months ago, FOOD, Busan)
        Meeting meeting3 = meetingRepository.save(Meeting.builder()
                .title("부산 맛집 탐방")
                .description("돼지국밥 뿌수기")
                .host(user3)
                .category(Category.FOOD)
                .location(new Location("부산", "해운대구", "해운대해변로", "", "", 0, 0))
                .meetingTime(LocalDateTime.now().minusMonths(2).withHour(12))
                .recruitDeadline(LocalDateTime.now().minusMonths(2).minusDays(1))
                .maxParticipants(20)
                .meetingStatus(MeetingStatus.FINISHED)
                .build());

        // Meeting 4: Hosted by 'user', attended by user2 and user3 (for testing 'received reviews')
        Meeting meeting4 = meetingRepository.save(Meeting.builder()
                .title("한강 피크닉")
                .description("치맥과 함께")
                .host(user)
                .category(Category.HOBBY)
                .location(new Location("서울", "영등포구", "여의동로", "", "", 0, 0))
                .meetingTime(LocalDateTime.now().minusWeeks(2).withHour(18))
                .recruitDeadline(LocalDateTime.now().minusWeeks(2).minusDays(1))
                .maxParticipants(8)
                .meetingStatus(MeetingStatus.FINISHED)
                .build());

        // === Create Sample Participations ===

        // 'user' participates in meetings 1, 2, 3
        meetingParticipantRepository.saveAll(List.of(
                createParticipant(meeting1, user),
                createParticipant(meeting2, user),
                createParticipant(meeting3, user)
        ));

        // 'user2' and 'user3' participate in meeting 4 (hosted by 'user')
        meetingParticipantRepository.saveAll(List.of(
                createParticipant(meeting4, user2),
                createParticipant(meeting4, user3)
        ));

        System.out.println("✅ Sample data for meeting statistics created.");
    }

    private MeetingParticipant createParticipant(Meeting meeting, UserAuth user) {
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setUser(user);
        participant.setApprovalStatus(ApprovalStatus.APPROVED);
        participant.setJoinedAt(meeting.getRecruitDeadline());
        return participant;
    }
}
