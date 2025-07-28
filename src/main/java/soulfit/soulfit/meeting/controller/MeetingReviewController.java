package soulfit.soulfit.meeting.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.MeetingReview;
import soulfit.soulfit.meeting.dto.MeetingReviewRequestDto;
import soulfit.soulfit.meeting.dto.MeetingReviewResponseDto;
import soulfit.soulfit.meeting.dto.MeetingReviewUpdateRequestDto;
import soulfit.soulfit.meeting.service.MeetingReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MeetingReviewController {

    private final MeetingReviewService meetingReviewService;

    //모임의 모든 리뷰(최신순)
    @GetMapping("meetings/{meetingId}/reviews")
    public ResponseEntity<Page<MeetingReviewResponseDto>> getAllReviews(@PathVariable Long meetingId,  Pageable pageable){
        Page<MeetingReview> meetingReviews = meetingReviewService.getAllReviews(meetingId, pageable);
        return ResponseEntity.ok(meetingReviews.map(MeetingReviewResponseDto::from));
    }

    //내가 쓴 모임 리뷰(최신순)
    @GetMapping("me/reviews")
    public ResponseEntity<Page<MeetingReviewResponseDto>> getUserReviews(@AuthenticationPrincipal UserAuth user, Pageable pageable){
        Page<MeetingReview> meetingReviews = meetingReviewService.getUserReviews(user, pageable);
        return ResponseEntity.ok(meetingReviews.map(MeetingReviewResponseDto::from));
    }


    @PostMapping("meetings/{meetingId}/reviews")
    public ResponseEntity<MeetingReviewResponseDto> createReview(@PathVariable Long meetingId, @ModelAttribute @Valid MeetingReviewRequestDto requestDto, @AuthenticationPrincipal UserAuth user){
        MeetingReview review = meetingReviewService.createReview(meetingId, requestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(MeetingReviewResponseDto.from(review));
    }

    @PatchMapping("reviews/{reviewId}")
    public ResponseEntity<MeetingReviewResponseDto> updateReview(@PathVariable Long reviewId, @ModelAttribute @Valid MeetingReviewUpdateRequestDto requestDto, @AuthenticationPrincipal UserAuth user){
        MeetingReview updatedMeetingReview = meetingReviewService.updateReview(requestDto, reviewId, user);
        return ResponseEntity.ok(MeetingReviewResponseDto.from(updatedMeetingReview));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, @AuthenticationPrincipal UserAuth user){
        meetingReviewService.deleteReview(reviewId, user);
        return ResponseEntity.noContent().build();
    }



}
