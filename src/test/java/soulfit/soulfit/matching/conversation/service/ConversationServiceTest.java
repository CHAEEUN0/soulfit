package soulfit.soulfit.matching.conversation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;
import soulfit.soulfit.matching.conversation.dto.ConversationRequestDto;
import soulfit.soulfit.matching.conversation.dto.ConversationResponseDto;
import soulfit.soulfit.matching.conversation.dto.UpdateRequestStatusDto;
import soulfit.soulfit.matching.conversation.repository.ConversationRequestRepository;
import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.service.NotificationService;
import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.UserProfile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ConversationServiceTest {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRequestRepository conversationRequestRepository;

    @Autowired
    private EntityManager em;

    @MockitoBean
    private NotificationService notificationService;

    private UserAuth fromUser;
    private UserAuth toUser;
    private UserProfile fromUserProfile;
    private UserProfile toUserProfile;

    @BeforeEach
    void setUp() {
        // given
        fromUser = new UserAuth("fromUser", "password", "from@test.com");
        toUser = new UserAuth("toUser", "password", "to@test.com");

        fromUserProfile = new UserProfile(fromUser, LocalDate.of(1990, 1, 1), Gender.MALE);
        fromUserProfile.setProfileImageUrl("http://example.com/fromUser.jpg");
        fromUser.setUserProfile(fromUserProfile);

        toUserProfile = new UserProfile(toUser, LocalDate.of(1995, 5, 5), Gender.FEMALE);
        toUserProfile.setProfileImageUrl("http://example.com/toUser.jpg");
        toUser.setUserProfile(toUserProfile);

        userRepository.save(fromUser);
        userRepository.save(toUser);
    }

    @Test
    @DisplayName("대화 신청을 성공적으로 생성한다")
    void createConversationRequest_Success() {
        // Given
        ConversationRequestDto requestDto = new ConversationRequestDto(toUser.getId(), "안녕하세요!");

        // When
        ConversationResponseDto responseDto = conversationService.createConversationRequest(fromUser, requestDto);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.message()).isEqualTo("안녕하세요!");
        assertThat(responseDto.status()).isEqualTo(RequestStatus.PENDING);

        // fromUser 정보 검증
        ConversationResponseDto.ConversationPartnerDto fromUserDto = responseDto.fromUser();
        assertThat(fromUserDto.userId()).isEqualTo(fromUser.getId());
        assertThat(fromUserDto.nickname()).isEqualTo(fromUser.getUsername());
        assertThat(fromUserDto.age()).isEqualTo(Period.between(fromUserProfile.getBirthDate(), LocalDate.now()).getYears());
        assertThat(fromUserDto.profileImageUrl()).isEqualTo(fromUserProfile.getProfileImageUrl());

        // toUser 정보 검증
        ConversationResponseDto.ConversationPartnerDto toUserDto = responseDto.toUser();
        assertThat(toUserDto.userId()).isEqualTo(toUser.getId());
        assertThat(toUserDto.nickname()).isEqualTo(toUser.getUsername());
        assertThat(toUserDto.age()).isEqualTo(Period.between(toUserProfile.getBirthDate(), LocalDate.now()).getYears());
        assertThat(toUserDto.profileImageUrl()).isEqualTo(toUserProfile.getProfileImageUrl());


        ConversationRequest savedRequest = conversationRequestRepository.findById(responseDto.id()).get();
        assertThat(savedRequest.getFromUser().getId()).isEqualTo(fromUser.getId());

        // NotificationService 호출 검증
        verify(notificationService, times(1)).sendNotification(
                eq(fromUser),
                eq(toUser),
                eq(NotificationType.CONVERSATION_REQUEST),
                eq("새로운 대화 신청"),
                eq(fromUser.getUsername() + "님께서 대화를 신청하셨습니다."),
                anyLong()
        );
    }

    @Test
    @DisplayName("자기 자신에게 대화 신청 시 예외가 발생한다")
    void createConversationRequest_Fail_SelfRequest() {
        // Given
        ConversationRequestDto requestDto = new ConversationRequestDto(fromUser.getId(), "나에게 보내는 메시지");

        // When & Then
        assertThatThrownBy(() -> conversationService.createConversationRequest(fromUser, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자기 자신에게 대화를 신청할 수 없습니다.");

        // NotificationService가 호출되지 않았음을 검증
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("이미 PENDING 상태인 요청이 존재할 경우 예외가 발생한다")
    void createConversationRequest_Fail_AlreadyPending() {
        // Given
        conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "첫 번째 요청"));
        reset(notificationService); // 첫 번째 요청으로 인한 호출 초기화

        ConversationRequestDto duplicateRequestDto = new ConversationRequestDto(toUser.getId(), "두 번째 요청");

        // When & Then
        assertThatThrownBy(() -> conversationService.createConversationRequest(fromUser, duplicateRequestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 처리 대기 중인 대화 신청이 존재합니다.");

        // NotificationService가 호출되지 않았음을 검증
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("받은 대화 신청을 성공적으로 수락한다")
    void updateRequestStatus_Accept_Success() {
        // Given
        ConversationResponseDto createdDto = conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "수락 테스트"));
        reset(notificationService); // createConversationRequest 호출로 인한 호출 초기화

        UpdateRequestStatusDto statusDto = new UpdateRequestStatusDto("ACCEPTED");

        // When
        ConversationResponseDto updatedDto = conversationService.updateRequestStatus(createdDto.id(), toUser, statusDto);

        // Then
        assertThat(updatedDto.status()).isEqualTo(RequestStatus.ACCEPTED);
        assertThat(updatedDto.fromUser().userId()).isEqualTo(fromUser.getId());
        assertThat(updatedDto.toUser().userId()).isEqualTo(toUser.getId());

        // NotificationService 호출 검증
        verify(notificationService, times(1)).sendNotification(
                eq(toUser),
                eq(fromUser),
                eq(NotificationType.APPROVED),
                eq("대화 신청 수락"),
                eq(toUser.getUsername() + "님께서 대화 신청을 수락하셨습니다."),
                anyLong()
        );
    }

    @Test
    @DisplayName("권한 없는 사용자가 상태 변경 시 예외가 발생한다")
    void updateRequestStatus_Fail_NoPermission() {
        // Given
        ConversationResponseDto createdDto = conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "권한 테스트"));
        reset(notificationService); // createConversationRequest 호출로 인한 호출 초기화

        UpdateRequestStatusDto statusDto = new UpdateRequestStatusDto("ACCEPTED");
        UserAuth otherUser = new UserAuth("otherUser", "pw", "other@test.com");
        otherUser.setUserProfile(new UserProfile(otherUser, LocalDate.of(2000, 1, 1), Gender.MALE));
        userRepository.save(otherUser);


        // When & Then
        assertThatThrownBy(() -> conversationService.updateRequestStatus(createdDto.id(), otherUser, statusDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("요청을 처리할 권한이 없습니다.");

        // NotificationService가 호출되지 않았음을 검증
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("받은 PENDING 상태의 요청 목록을 조회한다")
    void getReceivedRequests_Success() {
        // Given
        UserAuth anotherSender = new UserAuth("anotherSender", "pw", "as@test.com");
        UserProfile anotherSenderProfile = new UserProfile(anotherSender, LocalDate.of(1988, 1, 1), Gender.MALE);
        anotherSenderProfile.setProfileImageUrl("http://example.com/another.jpg");
        anotherSender.setUserProfile(anotherSenderProfile);
        userRepository.save(anotherSender);

        conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "요청1"));
        conversationService.createConversationRequest(anotherSender, new ConversationRequestDto(toUser.getId(), "요청2"));

        // When
        List<ConversationResponseDto> receivedList = conversationService.getReceivedRequests(toUser, RequestStatus.PENDING);

        // Then
        assertThat(receivedList).hasSize(2);
        // 최신순으로 정렬되므로 anotherSender가 먼저 와야 함
        ConversationResponseDto firstRequest = receivedList.get(0);
        assertThat(firstRequest.toUser().userId()).isEqualTo(toUser.getId());
        assertThat(firstRequest.fromUser().userId()).isEqualTo(anotherSender.getId());
        assertThat(firstRequest.fromUser().nickname()).isEqualTo(anotherSender.getUsername());
        assertThat(firstRequest.fromUser().age()).isEqualTo(Period.between(anotherSenderProfile.getBirthDate(), LocalDate.now()).getYears());
        assertThat(firstRequest.fromUser().profileImageUrl()).isEqualTo(anotherSenderProfile.getProfileImageUrl());
    }

    @Test
    @DisplayName("보낸 PENDING 상태의 요청 목록을 조회한다")
    void getSentRequests_Success() {
        // Given
        conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "보낸 요청"));

        // When
        List<ConversationResponseDto> sentList = conversationService.getSentRequests(fromUser, RequestStatus.PENDING);

        // Then
        assertThat(sentList).hasSize(1);
        ConversationResponseDto sentRequest = sentList.get(0);
        assertThat(sentRequest.fromUser().userId()).isEqualTo(fromUser.getId());
        assertThat(sentRequest.toUser().userId()).isEqualTo(toUser.getId());
        assertThat(sentRequest.toUser().nickname()).isEqualTo(toUser.getUsername());
        assertThat(sentRequest.toUser().age()).isEqualTo(Period.between(toUserProfile.getBirthDate(), LocalDate.now()).getYears());
        assertThat(sentRequest.toUser().profileImageUrl()).isEqualTo(toUserProfile.getProfileImageUrl());
    }
}
