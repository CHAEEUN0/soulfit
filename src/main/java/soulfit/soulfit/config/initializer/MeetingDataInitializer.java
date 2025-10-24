package soulfit.soulfit.config.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.repository.MeetingKeywordRepository;
import soulfit.soulfit.meeting.repository.MeetingParticipantRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@Order(2) // Run after UserInitializer
@RequiredArgsConstructor
public class MeetingDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingKeywordRepository meetingKeywordRepository;

    @Override
    public void run(String... args) throws Exception {
        if (meetingRepository.count() > 0) {
            return; // Data already initialized
        }

        // Get users
        UserAuth user = userRepository.findByUsername("user").orElseThrow();
        UserAuth user2 = userRepository.findByUsername("user2").orElseThrow();
        UserAuth user3 = userRepository.findByUsername("user3").orElseThrow();

        // Fetch all keywords once
        List<Keyword> allKeywords = meetingKeywordRepository.findAll();

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
                .fee(10000)
                .feeDescription("음료 및 간식 제공")
                .supplies(List.of("편한 복장", "운동화"))
                .schedules(List.of("19:00 - 집결", "19:10 - 스트레칭", "19:30 - 런닝 시작", "20:30 - 마무리 스트레칭"))
                .keywords(getKeywords(allKeywords, "활발한", "신나는", "액티비티", "산책"))
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
                .fee(0)
                .feeDescription("스터디룸 비용은 각자 부담")
                .supplies(List.of("개인 노트북", "필기구"))
                .schedules(List.of("14:00 - 16:00: 문제 풀이", "16:00 - 17:00: 코드 리뷰"))
                .keywords(getKeywords(allKeywords, "계획적", "차분한", "I성향", "실내중심"))
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
                .fee(25000)
                .feeDescription("1인 1메뉴 필수")
                .supplies(List.of("배고픈 위장", "활발한 장"))
                .schedules(List.of("12:00 - 1차", "14:00 - 2차", "16:00 - 카페"))
                .keywords(getKeywords(allKeywords, "즉흥적", "맛집", "술모임", "E성향"))
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
                .fee(15000)
                .feeDescription("치킨, 맥주 및 돗자리 제공")
                .supplies(List.of("개인 컵", "담요"))
                .schedules(List.of("18:00 - 여의나루역 집결", "18:30 - 치맥 타임", "21:00 - 정리 및 해산"))
                .keywords(getKeywords(allKeywords, "편안한", "힐링", "가벼운", "실외중심"))
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

    private Set<Keyword> getKeywords(List<Keyword> allKeywords, String... names) {
        List<String> nameList = Arrays.asList(names);
        return allKeywords.stream()
                .filter(keyword -> nameList.contains(keyword.getName()))
                .collect(Collectors.toSet());
    }
}
