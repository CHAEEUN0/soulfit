package soulfit.soulfit.matching.conversation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;
import soulfit.soulfit.matching.conversation.dto.ConversationRequestDto;
import soulfit.soulfit.matching.conversation.dto.ConversationResponseDto;
import soulfit.soulfit.matching.conversation.dto.UpdateRequestStatusDto;
import soulfit.soulfit.matching.conversation.repository.ConversationRequestRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;


import static org.assertj.core.api.Assertions.*;

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

    private UserAuth fromUser;
    private UserAuth toUser;

    @BeforeEach
    void setUp() {
        fromUser = new UserAuth("fromUser", "password", "from@test.com");
        toUser = new UserAuth("toUser", "password", "to@test.com");
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
        assertThat(responseDto.fromUserId()).isEqualTo(fromUser.getId());
        assertThat(responseDto.toUserId()).isEqualTo(toUser.getId());
        assertThat(responseDto.message()).isEqualTo("안녕하세요!");
        assertThat(responseDto.status()).isEqualTo(RequestStatus.PENDING);

        ConversationRequest savedRequest = conversationRequestRepository.findById(responseDto.id()).get();
        assertThat(savedRequest.getFromUser().getId()).isEqualTo(fromUser.getId());
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
    }

    @Test
    @DisplayName("이미 PENDING 상태인 요청이 존재할 경우 예외가 발생한다")
    void createConversationRequest_Fail_AlreadyPending() {
        // Given
        conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "첫 번째 요청"));
        ConversationRequestDto duplicateRequestDto = new ConversationRequestDto(toUser.getId(), "두 번째 요청");

        // When & Then
        assertThatThrownBy(() -> conversationService.createConversationRequest(fromUser, duplicateRequestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 처리 대기 중인 대화 신청이 존재합니다.");
    }

    @Test
    @DisplayName("받은 대화 신청을 성공적으로 수락한다")
    void updateRequestStatus_Accept_Success() {
        // Given
        ConversationResponseDto createdDto = conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "수락 테스트"));
        UpdateRequestStatusDto statusDto = new UpdateRequestStatusDto("ACCEPTED");

        // When
        ConversationResponseDto updatedDto = conversationService.updateRequestStatus(createdDto.id(), toUser, statusDto);

        // Then
        assertThat(updatedDto.status()).isEqualTo(RequestStatus.ACCEPTED);
    }

    @Test
    @DisplayName("권한 없는 사용자가 상태 변경 시 예외가 발생한다")
    void updateRequestStatus_Fail_NoPermission() {
        // Given
        ConversationResponseDto createdDto = conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "권한 테스트"));
        UpdateRequestStatusDto statusDto = new UpdateRequestStatusDto("ACCEPTED");
        UserAuth otherUser = userRepository.save(new UserAuth("otherUser", "pw", "other@test.com"));

        // When & Then
        assertThatThrownBy(() -> conversationService.updateRequestStatus(createdDto.id(), otherUser, statusDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("요청을 찾을 수 없거나 처리할 권한이 없습니다.");
    }

    @Test
    @DisplayName("받은 PENDING 상태의 요청 목록을 조회한다")
    void getReceivedRequests_Success() {
        // Given
        conversationService.createConversationRequest(fromUser, new ConversationRequestDto(toUser.getId(), "요청1"));
        conversationService.createConversationRequest(userRepository.save(new UserAuth("anotherSender", "pw", "as@test.com")), new ConversationRequestDto(toUser.getId(), "요청2"));

        // When
        List<ConversationResponseDto> receivedList = conversationService.getReceivedRequests(toUser, RequestStatus.PENDING);

        // Then
        assertThat(receivedList).hasSize(2);
        assertThat(receivedList.get(0).toUserId()).isEqualTo(toUser.getId());
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
        assertThat(sentList.get(0).fromUserId()).isEqualTo(fromUser.getId());
    }
}
