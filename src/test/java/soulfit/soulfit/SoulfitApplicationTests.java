package soulfit.soulfit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Location;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.dto.MeetingRequest;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.meeting.service.MeetingService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SoulfitApplicationTests {

	@Autowired
	private MeetingService meetingService;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private UserRepository userRepository;

	UserAuth host;
	@BeforeEach
	void 기본() {
		userRepository.deleteAll();
		host = new UserAuth("dssdds", "1234", "2@es.d");
		userRepository.save(host);
	}
	@Test
	void 모임생성(){



		Location location = Location.builder()
				.city("서울")
				.roadAddress("ㅇㅇ로")
				.build();

		MeetingRequest request = MeetingRequest.builder()
				.title("스터디 같이 하실 분")
				.description("같이 토익 스터디 하실분 구해요")
				.category(Category.STUDY)
				.location(location)
				.fee(7000)
				.maxParticipants(3)
				.meetingTime(LocalDateTime.of(2025, 7, 10, 13, 0))
				.recruitDeadline(LocalDateTime.of(2026, 7, 30, 0, 0))
				.build();

		Long meetingId = meetingService.createMeeting(host, request);

		Meeting meeting = meetingRepository.findById(meetingId)
				.orElseThrow(() -> new RuntimeException("모임 없음"));

		assertThat("스터디 같이 하실 분").isEqualTo(meeting.getTitle());

	}

	@Test
	void 모임수정(){

		MeetingRequest request = MeetingRequest.builder()
				.title("수정").build();

		Meeting meeting = meetingRepository.save(Meeting.builder()
				.title("초기 제목")
				.host(host)
				.meetingTime(LocalDateTime.now().plusDays(1))
				.recruitDeadline(LocalDateTime.now().plusHours(12))
				.maxParticipants(10)
				.fee(1000)
				.location(new Location("서울", "ddd로", "4544",  37.12, 127.56))
				.category(Category.WORKOUT)
				.currentParticipants(1)
				.build());


		meetingService.updateMeeting(meeting.getId(), request, host);

		Meeting updated = meetingRepository.findById(meeting.getId()).orElseThrow();
		assertThat("수정").isEqualTo(updated.getTitle());

	}
}
