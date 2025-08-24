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
import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.service.NotificationService;
import soulfit.soulfit.profile.domain.UserProfile;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.Period;
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
    private final NotificationService notificationService;

    @Transactional
    public ConversationResponseDto createConversationRequest(UserAuth fromUser, ConversationRequestDto requestDto) {
        log.debug("Attempting to create conversation request from user {} to user {}", fromUser.getId(), requestDto.toUserId());
        if (Objects.equals(fromUser.getId(), requestDto.toUserId())) {
            log.warn("User {} tried to request a conversation with themselves.", fromUser.getId());
            throw new IllegalArgumentException("자기 자신에게 대화를 신청할 수 없습니다.");
        }

        boolean alreadyExists = conversationRequestRepository.existsByFromUserIdAndToUserIdAndStatus(
                fromUser.getId(), requestDto.toUserId(), RequestStatus.PENDING);
        if (alreadyExists) {
            log.warn("Duplicate conversation request from user {} to user {}.", fromUser.getId(), requestDto.toUserId());
            throw new IllegalStateException("이미 처리 대기 중인 대화 신청이 존재합니다.");
        }

        UserAuth toUser = userRepository.findByIdWithProfile(requestDto.toUserId())
                .orElseThrow(() -> {
                    log.warn("Could not find user with ID: {}", requestDto.toUserId());
                    return new EntityNotFoundException("상대방 사용자를 찾을 수 없습니다. ID: " + requestDto.toUserId());
                });

        ConversationRequest newRequest = ConversationRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .message(requestDto.message())
                .build();

        ConversationRequest savedRequest = conversationRequestRepository.save(newRequest);

        String notificationTitle = "새로운 대화 신청";
        String notificationBody = fromUser.getUsername() + "님께서 대화를 신청하셨습니다.";
        notificationService.sendNotification(
                fromUser,
                toUser,
                NotificationType.CONVERSATION_REQUEST,
                notificationTitle,
                notificationBody,
                savedRequest.getId()
        );

        log.info("대화 신청이 생성되었습니다. from: {}, to: {}", fromUser.getId(), toUser.getId());
        return toConversationResponseDto(savedRequest);
    }

    @Transactional
    public ConversationResponseDto updateRequestStatus(Long requestId, UserAuth currentUser, UpdateRequestStatusDto statusDto) {
        log.debug("User {} attempting to update request {} with status {}", currentUser.getId(), requestId, statusDto.status());
        ConversationRequest request = conversationRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Conversation request not found for ID: {}. User: {}", requestId, currentUser.getId());
                    return new EntityNotFoundException("요청을 찾을 수 없거나 처리할 권한이 없습니다. ID: " + requestId);
                });

        if (!request.getToUser().getId().equals(currentUser.getId())) {
            log.error("User {} attempted to process request {} which belongs to user {}. Access denied.",
                    currentUser.getId(), requestId, request.getToUser().getId());
            throw new IllegalStateException("요청을 처리할 권한이 없습니다.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            log.warn("User {} attempted to process already handled request {}. Current status: {}",
                    currentUser.getId(), requestId, request.getStatus());
            throw new IllegalStateException("이미 처리된 요청입니다. 현재 상태: " + request.getStatus());
        }

        RequestStatus newStatus = RequestStatus.valueOf(statusDto.status().toUpperCase());
        request.updateStatus(newStatus);

        if (newStatus == RequestStatus.ACCEPTED) {
            // TODO: 채팅방 생성 기능 완료 후, 아래 주석을 해제하고 실제 로직을 연동해야 함.
            // ChatRoom newChatRoom = chatService.createChatRoom(request.getFromUser(), request.getToUser());
            // request.setChatRoomId(newChatRoom.getId());
            log.info("대화 신청 #{}이(가) 수락되었습니다. By user {}", requestId, currentUser.getId());
            String notificationTitle = "대화 신청 수락";
            String notificationBody = currentUser.getUsername() + "님께서 대화 신청을 수락하셨습니다.";
            notificationService.sendNotification(
                    currentUser,
                    request.getFromUser(),
                    NotificationType.APPROVED,
                    notificationTitle,
                    notificationBody,
                    request.getId()
            );
        } else {
            log.info("대화 신청 #{}이(가) 거절되었습니다. By user {}", requestId, currentUser.getId());
        }

        return toConversationResponseDto(request);
    }

    public List<ConversationResponseDto> getReceivedRequests(UserAuth user, RequestStatus status) {
        log.debug("Fetching received conversation requests for user {} with status {}", user.getId(), status);
        List<ConversationRequest> requests = conversationRequestRepository.findByToUserAndStatusWithProfiles(user, status);
        log.info("Found {} received conversation requests for user {}", requests.size(), user.getId());
        return requests.stream()
                .map(this::toConversationResponseDto)
                .collect(Collectors.toList());
    }

    public List<ConversationResponseDto> getSentRequests(UserAuth user, RequestStatus status) {
        log.debug("Fetching sent conversation requests for user {} with status {}", user.getId(), status);
        List<ConversationRequest> requests = conversationRequestRepository.findByFromUserAndStatusWithProfiles(user, status);
        log.info("Found {} sent conversation requests for user {}", requests.size(), user.getId());
        return requests.stream()
                .map(this::toConversationResponseDto)
                .collect(Collectors.toList());
    }

    private ConversationResponseDto toConversationResponseDto(ConversationRequest request) {
        ConversationResponseDto.ConversationPartnerDto fromUserDto = toPartnerDto(request.getFromUser());
        ConversationResponseDto.ConversationPartnerDto toUserDto = toPartnerDto(request.getToUser());

        return new ConversationResponseDto(
                request.getId(),
                fromUserDto,
                toUserDto,
                request.getMessage(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    private ConversationResponseDto.ConversationPartnerDto toPartnerDto(UserAuth user) {
        UserProfile profile = user.getUserProfile();
        int age = 0;
        if (profile != null && profile.getBirthDate() != null) {
            age = Period.between(profile.getBirthDate(), LocalDate.now()).getYears();
        }

        String profileImageUrl = (profile != null) ? profile.getProfileImageUrl() : null;

        return new ConversationResponseDto.ConversationPartnerDto(
                user.getId(),
                user.getUsername(), // 또는 profile.getNickname() 등
                age,
                profileImageUrl
        );
    }
}
