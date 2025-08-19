
package soulfit.soulfit.matching.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewRequestDto {

    @NotNull(message = "리뷰 대상자 ID는 필수입니다.")
    private Long revieweeId;

    @NotNull(message = "대화 요청 ID는 필수입니다.")
    private Long conversationRequestId;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력할 수 있습니다.")
    private String comment;

    @NotNull(message = "키워드는 필수입니다.")
    @Size(max = 2, message = "키워드는 최대 2개까지 선택할 수 있습니다.")
    private List<String> keywords;
}
