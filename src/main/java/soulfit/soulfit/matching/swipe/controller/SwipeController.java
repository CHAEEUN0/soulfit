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
}
