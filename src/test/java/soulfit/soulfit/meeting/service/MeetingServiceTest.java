package soulfit.soulfit.meeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.dto.MeetingRequestDto;
import soulfit.soulfit.meeting.dto.MeetingResponseDto;
import soulfit.soulfit.meeting.dto.MeetingUpdateRequestDto;
import soulfit.soulfit.meeting.dto.SearchFilter;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MeetingServiceTest {

    @Autowired UserRepository userRepository;
    @Autowired MeetingService meetingService;
    @Autowired MeetingRepository meetingRepository;
    @Autowired ObjectMapper objectMapper;




    @Test
    void 모임_생성(){
        UserAuth user = new UserAuth("test", "1234", "test@Test.com");
        userRepository.save(user);

        MeetingRequestDto requestDto = MeetingRequestDto.builder()
                .title("테스트")
                .description("테스트 내용")
                .fee(10000)
                .feeDescription("교재비")
                .duration(60)
                .canPickup(true)
                .keywordIds(List.of(1L, 2L, 3L))
                .category(Category.STUDY)
                .location(new Location("경기도", "수원시", "광교산로 154-42", ".", "16227", 1.1, 2.2))
                .meetingTime(LocalDateTime.of(2026, 8, 10, 13, 00))
                .recruitDeadline(LocalDateTime.of(2026, 8, 8, 8, 30))
                .maxParticipants(10)
                .build();


        Meeting meeting = meetingService.createMeeting(requestDto, user);

        assertThat(meeting.getTitle()).isEqualTo("테스트");

        Set<Long> savedKeywordIds = meeting.getKeywords().stream()
                .map(Keyword::getId)
                .collect(Collectors.toSet());
        assertThat(savedKeywordIds).containsExactlyInAnyOrderElementsOf(List.of(1L, 2L, 3L));

        Set<String> savedKeywordNames = meeting.getKeywords().stream()
                .map(Keyword::getName)
                .collect(Collectors.toSet());
        assertThat(savedKeywordNames).containsExactlyInAnyOrderElementsOf(List.of("즉흥적", "계획적", "활발한"));

    }

    @Test
    @Transactional
    void 모임_수정() throws IOException {
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

        InputStream fis1 = getClass().getResourceAsStream("/test1.jpeg");
        InputStream fis2 = getClass().getResourceAsStream("/test2.png");

        MockMultipartFile multipartFile1 = new MockMultipartFile(
                "images", "test1.jpeg", "image/jpeg", fis1);
        MockMultipartFile multipartFile2 = new MockMultipartFile(
                "images", "test2.png", "image/png", fis2);


        List<MultipartFile> files = List.of(multipartFile1, multipartFile2);

        MeetingUpdateRequestDto updateDto =
                MeetingUpdateRequestDto
                        .builder()
                        .fee(0)
                        .keywordIds(List.of(5L))
                        .schedules(List.of("러닝", "식사"))
                        .supplies(List.of("러닝화", "물"))
                        .images(files)
                        .build();

        Meeting updated = meetingService.updateMeeting(meeting.getId(), updateDto, user);

        assertThat(updated.getFee()).isEqualTo(0);
        assertThat(updated.getTitle()).isEqualTo("러닝해요");

        Set<String> savedKeywordNames = updated.getKeywords().stream()
                .map(Keyword::getName)
                .collect(Collectors.toSet());
        assertThat(savedKeywordNames).containsExactlyInAnyOrderElementsOf(List.of("신나는"));

        // Assertions for the detailed DTO returned by getMeetingById
        MeetingResponseDto responseDto = meetingService.getMeetingById(updated.getId());
        assertThat(responseDto.getPricePerPerson()).isEqualTo(0);
        assertThat(responseDto.getHostName()).isEqualTo(user.getUsername());
        assertThat(responseDto.getSchedules()).containsExactly("러닝", "식사");
        assertThat(responseDto.getSupplies()).containsExactly("러닝화", "물");
        assertThat(responseDto.getImageUrls().size()).isEqualTo(2);

        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDto));

    }

    @Test
    void 모임_삭제(){
        UserAuth user = new UserAuth("test", "1234", "test@Test.com");
        userRepository.save(user);
        Meeting meeting = meetingRepository.save(Meeting.createMeeting(
                MeetingRequestDto.builder()
                        .title("카페 투어")
                        .description("카페 가요")
                        .category(Category.FOOD)
                        .location(new Location("인천", "서구", "ㅇㅇ빌딩", "2층", "56869", 35.1, 129.1))
                        .fee(30000)
                        .feeDescription("카페비")
                        .canPickup(false)
                        .meetingTime(LocalDateTime.now().plusDays(1))
                        .duration(60)
                        .recruitDeadline(LocalDateTime.now().plusHours(2))
                        .maxParticipants(3)
                        .build(),
                user));

        meetingService.deleteMeeting(meeting.getId(), user);

        assertThat(meetingRepository.findById(meeting.getId()).isPresent()).isFalse();

    }

    @Test
    void 조건검색(){
        UserAuth user = new UserAuth("test", "1234", "test@Test.com");
        userRepository.save(user);

        Random random = new Random();

        for (int i = 1; i <= 50; i++) {
            Meeting meeting = Meeting.builder()
                    .title("모임" + i)
                    .description("더미 데이터")
                    .category(Category.STUDY)
                    .meetingStatus(MeetingStatus.OPEN)
                    .location(new Location(
                            i % 2 == 0 ? "인천" : "부산",
                            i % 5 == 0 ? "서구" : "중구",
                            "주소" + i, "상세" + i, "12345", 37.0 + i, 127.0 + i))
                    .host(user)
                    .fee(5000 + (i * 1000)) // 6000, 7000, ... 다양하게
                    .meetingTime(LocalDateTime.of(2025, 8, (i % 28) + 1, 19, 0)) // 8월 1~28일
                    .maxParticipants(10 + (i % 10)) // 10~19명
                    .canPickup(random.nextBoolean())
                    .build();
            meetingRepository.save(meeting);
        }

        System.out.println(meetingRepository.count());

        SearchFilter filter = SearchFilter.builder().city("인천").district("서구").minFee(20000).startDate(LocalDate.of(2025, 8, 10)).endDate(LocalDate.of(2025,8,30)).build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<MeetingResponseDto> result = meetingService.filterMeetings(filter, pageable);


        System.out.println("총 결과 개수 = " + result.getTotalElements());
        System.out.println("첫 번째 결과 = " + (result.getContent().isEmpty() ? "없음" : result.getContent().get(0).getTitle()));

        assertThat(result.getTotalElements()).isGreaterThan(0);
        assertThat(result.getContent()).hasSizeLessThanOrEqualTo(10);

        System.out.println("==== 검색 결과 리스트 ====");
        result.getContent().forEach(m -> {
            System.out.println("제목: " + m.getTitle()
                    + ", category: " + m.getCategory()
                    + ", status: " + m.getStatus());
        });

    }

}