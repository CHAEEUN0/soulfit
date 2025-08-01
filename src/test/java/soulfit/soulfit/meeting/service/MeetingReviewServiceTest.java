package soulfit.soulfit.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.community.post.PostService;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Location;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingReview;
import soulfit.soulfit.meeting.dto.MeetingRequestDto;
import soulfit.soulfit.meeting.dto.MeetingReviewRequestDto;
import soulfit.soulfit.meeting.dto.MeetingReviewResponseDto;
import soulfit.soulfit.meeting.dto.MeetingReviewUpdateRequestDto;
import soulfit.soulfit.meeting.repository.HostProfileRepository;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.meeting.repository.MeetingReviewRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MeetingReviewServiceTest {

    @Autowired MeetingRepository meetingRepository;

    @Autowired MeetingService meetingService;

    @Autowired MeetingReviewService meetingReviewService;

    @Autowired UserRepository userRepository;

    @Autowired ObjectMapper objectMapper;

    @Test
    @Transactional
    void 리뷰_생성() throws JsonProcessingException {
        UserAuth user = new UserAuth("test", "1234", "test@Test.com");
        userRepository.save(user);

        Meeting meeting = meetingRepository.save(meetingService.createMeeting(
                MeetingRequestDto.builder()
                        .title("러닝해요")
                        .description("러닝")
                        .location(new Location("서울", "여의도", "한강공원", ".", "55411", 11.11, 22.22))
                        .category(Category.WORKOUT)
                        .fee(10000)
                        .feeDescription("러닝 후 식사")
                        .duration(90)
                        .maxParticipants(20)
                        .meetingTime(LocalDateTime.of(2026, 8, 10, 13, 00))
                        .recruitDeadline(LocalDateTime.of(2026, 8, 8, 8, 30))
                        .keywordIds(List.of(11L, 20L))
                        .canPickup(false)
                        .build(), user
        ));

        MeetingReview review = meetingReviewService.createReview(meeting.getId(),
                MeetingReviewRequestDto.builder()
                        .meetingRating(5.0)
                        .hostRating(4.5)
                        .content("재밌어요")
                        .build(), user);

        assertThat(review.getMeetingRating()).isEqualTo(5.0);
        assertThat(review.getHostRating()).isEqualTo(4.5);
        assertThat(review.getContent()).isEqualTo("재밌어요");


    }

    @Test
    @Transactional
    void 리뷰_수정() throws IOException {
        UserAuth user = new UserAuth("test", "1234", "test@Test.com");
        userRepository.save(user);

        Meeting meeting = meetingRepository.save(meetingService.createMeeting(
                MeetingRequestDto.builder()
                        .title("러닝해요")
                        .description("러닝")
                        .location(new Location("서울", "여의도", "한강공원", ".", "55411", 11.11, 22.22))
                        .category(Category.WORKOUT)
                        .fee(10000)
                        .feeDescription("러닝 후 식사")
                        .duration(90)
                        .maxParticipants(20)
                        .meetingTime(LocalDateTime.of(2026, 8, 10, 13, 00))
                        .recruitDeadline(LocalDateTime.of(2026, 8, 8, 8, 30))
                        .keywordIds(List.of(11L, 20L))
                        .canPickup(false)
                        .build(), user
        ));

        MeetingReview review = meetingReviewService.createReview(meeting.getId(),
                MeetingReviewRequestDto.builder()
                        .meetingRating(5.0)
                        .hostRating(4.5)
                        .content("재밌어요")
                        .build(), user);

        /*InputStream fis1 = getClass().getResourceAsStream("/test1.jpeg");
        InputStream fis2 = getClass().getResourceAsStream("/test2.png");

        MockMultipartFile multipartFile1 = new MockMultipartFile(
                "images", "test1.jpeg", "image/jpeg", fis1);
        MockMultipartFile multipartFile2 = new MockMultipartFile(
                "images", "test2.png", "image/png", fis2);


        List<MultipartFile> files = List.of(multipartFile1, multipartFile2);*/

        MeetingReview updated = meetingReviewService.updateReview(MeetingReviewUpdateRequestDto.builder()
                .meetingRating(0.5)
                .content("굿")
                .build(), review.getId(), user);


        assertThat(updated.getMeetingRating()).isEqualTo(0.5);
        assertThat(updated.getContent()).contains("굿");

        MeetingReviewResponseDto responseDto = MeetingReviewResponseDto.from(updated);
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDto));

    }

}