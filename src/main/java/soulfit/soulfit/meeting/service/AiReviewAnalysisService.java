package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import soulfit.soulfit.meeting.client.AiMeetingClient;
import soulfit.soulfit.meeting.domain.MeetingReview;
import soulfit.soulfit.meeting.dto.ai.AiReviewRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiReviewResponseDto;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiReviewAnalysisService {

//    private final WebClient webClient;
    private final AiMeetingClient aiMeetingClient;

//    public AiReviewResponseDto analyzeReviews(List<MeetingReview> meetingReviews) {
//        List<AiReviewRequestDto.ReviewData> reviewDataList = meetingReviews.stream()
//            .map(review -> new AiReviewRequestDto.ReviewData(
//                review.getContent(),
//                review.getMeetingRating() != null ? review.getMeetingRating().intValue() : null,
//                review.getCreatedAt() != null ? review.getCreatedAt().format(DateTimeFormatter.ISO_INSTANT) : null
//            ))
//            .collect(Collectors.toList());
//
//        AiReviewRequestDto requestDto = new AiReviewRequestDto(reviewDataList);
//
//        return webClient.post()
//            .uri("/meeting/analyze-reviews")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(requestDto)
//            .retrieve()
//            .bodyToMono(AiReviewResponseDto.class)
//            .block(); // Blocking for simplicity, consider reactive approach for production
//    }

    public AiReviewResponseDto analyzeReviewsByRestTemplate(List<MeetingReview> meetingReviews) {
        List<AiReviewRequestDto.ReviewData> reviewDataList = meetingReviews.stream()
                .map(review -> new AiReviewRequestDto.ReviewData(
                        review.getContent(),
                        review.getMeetingRating() != null ? review.getMeetingRating().intValue() : null,
                        review.getCreatedAt() != null ? DateTimeFormatter.ISO_INSTANT.format(review.getCreatedAt().toInstant(ZoneOffset.UTC)) : null
                ))
                .toList();

        AiReviewRequestDto requestDto = new AiReviewRequestDto(reviewDataList);

        return aiMeetingClient.analyzeMeetingReview(requestDto);
    }
}
