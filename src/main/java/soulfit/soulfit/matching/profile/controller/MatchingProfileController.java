package soulfit.soulfit.matching.profile.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.profile.dto.MatchingProfileRequestDto;
import soulfit.soulfit.matching.profile.dto.MatchingProfileResponseDto;
import soulfit.soulfit.matching.profile.service.MatchingProfileService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching-profiles")
public class MatchingProfileController {

    private final MatchingProfileService matchingProfileService;

    // 자신의 매칭 프로필 수정
    @PutMapping
    public ResponseEntity<MatchingProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal UserAuth user,
            @RequestBody MatchingProfileRequestDto dto
    ) {
        return ResponseEntity.ok(matchingProfileService.updateMyProfile(user, dto));
    }

    // 다른 사용자의 매칭 프로필 조회
    @GetMapping("/{userId}")
    public ResponseEntity<MatchingProfileResponseDto> getProfileByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(matchingProfileService.getProfileByUserId(userId));
    }
}

