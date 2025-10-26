package soulfit.soulfit.matching.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import soulfit.soulfit.authentication.dto.MessageResponse;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.ai.AiApiResponseDto;
import soulfit.soulfit.matching.ai.AiMatchResponseDto;
import soulfit.soulfit.matching.ai.AiMatchService;
import soulfit.soulfit.matching.ai.dto.ClientAiMatchRequestDto;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class AiMatchingController {

    private final AiMatchService aiMatchService;

    @PostMapping("/ai-match")
    public ResponseEntity<?> getAiRecommendedUsers(@AuthenticationPrincipal UserAuth user, @RequestBody ClientAiMatchRequestDto requestDto) {
        Long targetUserId = user.getId();

        try {
            AiMatchResponseDto response = aiMatchService.getAiRecommendedUsers(targetUserId, requestDto.getCandidateUserIds());
            return ResponseEntity.ok(response.getAiMatchResults()); // Return the list directly as per spec
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(500).body(new MessageResponse("Error processing AI match request: " + e.getMessage()));
        }
    }
}
