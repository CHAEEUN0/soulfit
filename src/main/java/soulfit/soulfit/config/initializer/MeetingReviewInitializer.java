package soulfit.soulfit.config.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingReview;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.meeting.repository.MeetingReviewRepository;

import java.util.List;

@Component
@Profile("!test")
@Order(3) // Run after MeetingDataInitializer
@RequiredArgsConstructor
public class MeetingReviewInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingReviewRepository meetingReviewRepository;

    @Override
    public void run(String... args) throws Exception {
        if (meetingReviewRepository.count() > 0) {
            return; // Data already initialized
        }

        // Get users
        UserAuth user = userRepository.findByUsername("user").orElseThrow();
        UserAuth user2 = userRepository.findByUsername("user2").orElseThrow();

        // Get meetings by title (assuming titles are unique for this sample data)
        Meeting runningCrewMeeting = meetingRepository.findByTitle("저녁 런닝 크루").stream().findFirst().orElseThrow();
        Meeting foodMeeting = meetingRepository.findByTitle("부산 맛집 탐방").stream().findFirst().orElseThrow();
        Meeting picnicMeeting = meetingRepository.findByTitle("한강 피크닉").stream().findFirst().orElseThrow();

        // === Create Sample Reviews ===

        // Review 1: 'user' reviews the running crew meeting
        MeetingReview review1 = MeetingReview.builder()
                .meeting(runningCrewMeeting)
                .user(user)
                .meetingRating(5.0)
                .hostRating(4.5)
                .content("모임장이 친절하고 재밌었어요!")
                .build();

        // Review 2: 'user' reviews the food meeting
        MeetingReview review2 = MeetingReview.builder()
                .meeting(foodMeeting)
                .user(user)
                .meetingRating(4.0)
                .hostRating(4.0)
                .content("부산 최고 맛집 인정합니다.")
                .build();

        // Review 3: 'user2' reviews the picnic meeting (hosted by 'user')
        MeetingReview review3 = MeetingReview.builder()
                .meeting(picnicMeeting)
                .user(user2)
                .meetingRating(4.5)
                .hostRating(5.0) // Rating for 'user' as a host
                .content("준비성이 철저하셔서 좋았어요!")
                .build();

        meetingReviewRepository.saveAll(List.of(review1, review2, review3));

        System.out.println("✅ Sample data for meeting reviews created.");
    }
}
