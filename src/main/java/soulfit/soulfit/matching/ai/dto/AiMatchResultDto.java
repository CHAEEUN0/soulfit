package soulfit.soulfit.matching.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchResultDto {
    private Long userId;
    private Double matchScore;
    private String matchReason;
}
