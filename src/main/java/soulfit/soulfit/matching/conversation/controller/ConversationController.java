package soulfit.soulfit.matching.conversation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.conversation.domain.RequestStatus;
import soulfit.soulfit.matching.conversation.dto.ConversationRequestDto;
import soulfit.soulfit.matching.conversation.dto.ConversationResponseDto;
import soulfit.soulfit.matching.conversation.dto.UpdateRequestStatusDto;
import soulfit.soulfit.matching.conversation.service.ConversationService;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 대화 신청 보내기
     * @param currentUser 현재 인증된 사용자
     * @param requestDto 요청 DTO
     * @return 생성된 요청 정보
     */
    @PostMapping("/requests")
    public ResponseEntity<ConversationResponseDto> createConversationRequest(
            @AuthenticationPrincipal UserAuth currentUser,
            @Valid @RequestBody ConversationRequestDto requestDto) {
        ConversationResponseDto responseDto = conversationService.createConversationRequest(currentUser, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 대화 신청 상태 변경 (수락/거절)
     * @param requestId 대상 요청 ID
     * @param currentUser 현재 인증된 사용자 (요청을 받은 사람이어야 함)
     * @param statusDto 상태 변경 DTO
     * @return 업데이트된 요청 정보
     */
    @PatchMapping("/requests/{requestId}")
    public ResponseEntity<ConversationResponseDto> updateRequestStatus(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserAuth currentUser,
            @Valid @RequestBody UpdateRequestStatusDto statusDto) {
        ConversationResponseDto responseDto = conversationService.updateRequestStatus(requestId, currentUser, statusDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 받은 대화 신청 목록 조회
     * @param currentUser 현재 인증된 사용자
     * @param status 조회할 상태 (PENDING, ACCEPTED, REJECTED), 기본값 PENDING
     * @return 요청 목록
     */
    @GetMapping("/requests/received")
    public ResponseEntity<List<ConversationResponseDto>> getReceivedRequests(
            @AuthenticationPrincipal UserAuth currentUser,
            @RequestParam(defaultValue = "PENDING") RequestStatus status) {
        List<ConversationResponseDto> responseDtos = conversationService.getReceivedRequests(currentUser, status);
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * 보낸 대화 신청 목록 조회
     * @param currentUser 현재 인증된 사용자
     * @param status 조회할 상태 (PENDING, ACCEPTED, REJECTED), 기본값 PENDING
     * @return 요청 목록
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<List<ConversationResponseDto>> getSentRequests(
            @AuthenticationPrincipal UserAuth currentUser,
            @RequestParam(defaultValue = "PENDING") RequestStatus status) {
        List<ConversationResponseDto> responseDtos = conversationService.getSentRequests(currentUser, status);
        return ResponseEntity.ok(responseDtos);
    }
}
