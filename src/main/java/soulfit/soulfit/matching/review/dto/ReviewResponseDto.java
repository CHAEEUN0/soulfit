
package soulfit.soulfit.matching.review.dto;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.matching.review.domain.Review;
import soulfit.soulfit.matching.review.domain.ReviewKeyword;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReviewResponseDto {

    private final Long id;
    private final String comment;
    private final List<String> keywords;
    private final LocalDateTime createdAt;
    private final ReviewerDto reviewer;

    @Getter
    @Builder
    public static class ReviewerDto {
        private final Long id;
        private final String nickname;
    }

    public static ReviewResponseDto from(Review review) {
        ReviewerDto reviewerDto = ReviewerDto.builder()
                .id(review.getReviewer().getId())
                .nickname(review.getReviewer().getUsername())
                .build();

        return ReviewResponseDto.builder()
                .id(review.getId())
                .comment(review.getComment())
                .keywords(review.getKeywords().stream()
                        .map(ReviewKeyword::getKeyword)
                        .collect(Collectors.toList()))
                .createdAt(review.getCreatedAt())
                .reviewer(reviewerDto)
                .build();
    }
}
