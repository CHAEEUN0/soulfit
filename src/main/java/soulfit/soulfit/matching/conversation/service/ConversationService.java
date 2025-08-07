package soulfit.soulfit.matching.conversation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;
import soulfit.soulfit.matching.conversation.dto.ConversationRequestDto;
import soulfit.soulfit.matching.conversation.dto.ConversationResponseDto;
import soulfit.soulfit.matching.conversation.dto.UpdateRequestStatusDto;
import soulfit.soulfit.matching.conversation.repository.ConversationRequestRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

    private final ConversationRequestRepository conversationRequestRepository;
    private final UserRepository userRepository;

    /**
     * 대화 신청 생성
     */
    @Transactional
    public ConversationResponseDto createConversationRequest(UserAuth fromUser, ConversationRequestDto requestDto) {
        // 1. 자기 자신에게 요청하는지 확인
        if (Objects.equals(fromUser.getId(), requestDto.toUserId())) {
            throw new IllegalArgumentException("자기 자신에게 대화를 신청할 수 없습니다.");
        }

        // 2. PENDING 상태의 중복 요청 확인
        boolean alreadyExists = conversationRequestRepository.existsByFromUserIdAndToUserIdAndStatus(
                fromUser.getId(), requestDto.toUserId(), RequestStatus.PENDING);
        if (alreadyExists) {
            throw new IllegalStateException("이미 처리 대기 중인 대화 신청이 존재합니다.");
        }

        // 3. 요청받는 사용자 존재 여부 확인
        UserAuth toUser = userRepository.findById(requestDto.toUserId())
                .orElseThrow(() -> new EntityNotFoundException("상대방 사용자를 찾을 수 없습니다. ID: " + requestDto.toUserId()));

        // 4. 엔티티 생성 및 저장
        ConversationRequest newRequest = ConversationRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .message(requestDto.message())
                .build();

        ConversationRequest savedRequest = conversationRequestRepository.save(newRequest);

        log.info("대화 신청이 생성되었습니다. from: {}, to: {}", fromUser.getId(), toUser.getId());
        return ConversationResponseDto.from(savedRequest);
    }

    /**
     * 대화 신청 상태 변경 (수락/거절)
     */
    @Transactional
    public ConversationResponseDto updateRequestStatus(Long requestId, UserAuth currentUser, UpdateRequestStatusDto statusDto) {
        // 1. 요청 존재 및 권한 확인 (요청받은 사람만 변경 가능)
        ConversationRequest request = conversationRequestRepository.findByIdAndToUserId(requestId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("요청을 찾을 수 없거나 처리할 권한이 없습니다. ID: " + requestId));

        // 2. 이미 처리된 요청인지 확인
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다. 현재 상태: " + request.getStatus());
        }

        // 3. 상태 업데이트
        RequestStatus newStatus = RequestStatus.valueOf(statusDto.status().toUpperCase());
        request.updateStatus(newStatus);

        // 4. (미래 확장 지점) 채팅방 생성 로직
        if (newStatus == RequestStatus.ACCEPTED) {
            // TODO: 채팅방 생성 기능 완료 후, 아래 주석을 해제하고 실제 로직을 연동해야 함.
            // ChatRoom newChatRoom = chatService.createChatRoom(request.getFromUser(), request.getToUser());
            // request.setChatRoomId(newChatRoom.getId());
            log.info("대화 신청 #{}이(가) 수락되었습니다. (채팅방 생성 대기)", requestId);
        } else {
            log.info("대화 신청 #{}이(가) 거절되었습니다.", requestId);
        }

        return ConversationResponseDto.from(request);
    }

    /**
     * 받은 대화 신청 목록 조회
     */
    public List<ConversationResponseDto> getReceivedRequests(UserAuth user, RequestStatus status) {
        List<ConversationRequest> requests = conversationRequestRepository.findByToUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status);
        return requests.stream()
                .map(ConversationResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 보낸 대화 신청 목록 조회
     */
    public List<ConversationResponseDto> getSentRequests(UserAuth user, RequestStatus status) {
        List<ConversationRequest> requests = conversationRequestRepository.findByFromUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status);
        return requests.stream()
                .map(ConversationResponseDto::from)
                .collect(Collectors.toList());
    }
}
