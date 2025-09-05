
package soulfit.soulfit.matching.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.review.dto.ReviewRequestDto;
import soulfit.soulfit.matching.review.dto.ReviewResponseDto;
import soulfit.soulfit.matching.review.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @AuthenticationPrincipal UserAuth currentUser,
            @Valid @RequestBody ReviewRequestDto requestDto) {
        ReviewResponseDto responseDto = reviewService.createReview(currentUser, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsForUser(@PathVariable Long userId) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsForUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews(@AuthenticationPrincipal UserAuth currentUser) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByMe(currentUser);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/keywords")
    public ResponseEntity<List<String>> getAllKeywords() {
        List<String> keywords = reviewService.getAllKeywords();
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/user/{userId}/keywords/summary")
    public ResponseEntity<List<String>> getTopKeywordsForUser(@PathVariable Long userId) {
        int keywordLimit = 3; // 상위 3개의 키워드를 가져옴
        List<String> keywords = reviewService.getTopKeywordsForUser(userId, keywordLimit);
        return ResponseEntity.ok(keywords);
    }
}
