package soulfit.soulfit.matching.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchResultDto {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("match_score")
    private Double matchScore;

    @JsonProperty("match_reason")
    private String matchReason;
}
