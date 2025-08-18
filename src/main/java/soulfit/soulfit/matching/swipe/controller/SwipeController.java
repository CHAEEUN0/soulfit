package soulfit.soulfit.matching.swipe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.swipe.dto.MatchResponse;
import soulfit.soulfit.matching.swipe.dto.SwipeRequest;
import soulfit.soulfit.matching.swipe.dto.SwipeUserResponse;
import soulfit.soulfit.matching.swipe.service.SwipeService;

import java.util.List;
import soulfit.soulfit.matching.swipe.dto.SwipeTargetUserResponse;

@RestController
@RequestMapping("/api/swipes")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping
    public ResponseEntity<MatchResponse> swipe(
            @AuthenticationPrincipal UserAuth user,
            @RequestBody @Valid SwipeRequest swipeRequest) {

        MatchResponse response = swipeService.performSwipe(user, swipeRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/liked-by-me")
    public ResponseEntity<List<SwipeUserResponse>> getMyLikedUsers(@AuthenticationPrincipal UserAuth user) {
        List<SwipeUserResponse> users = swipeService.getMyLikedUsers(user);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/liked-me")
    public ResponseEntity<List<SwipeUserResponse>> getUsersWhoLikedMe(@AuthenticationPrincipal UserAuth user) {
        List<SwipeUserResponse> users = swipeService.getUsersWhoLikedMe(user);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/targets")
    public ResponseEntity<List<SwipeTargetUserResponse>> getPotentialSwipeTargets(
            @AuthenticationPrincipal UserAuth currentUser,
            @RequestParam(required = false) Double currentUserLatitude,
            @RequestParam(required = false) Double currentUserLongitude,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer minHeight,
            @RequestParam(required = false) Integer maxHeight,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Double maxDistanceInKm,
            @RequestParam(required = false) String smokingStatus,
            @RequestParam(required = false) String drinkingStatus
    ) {
        List<SwipeTargetUserResponse> targets = swipeService.getPotentialSwipeTargets(
                currentUser,
                currentUserLatitude,
                currentUserLongitude,
                region,
                minHeight, maxHeight,
                minAge, maxAge,
                maxDistanceInKm,
                smokingStatus,
                drinkingStatus
        );
        return ResponseEntity.ok(targets);
    }
}
