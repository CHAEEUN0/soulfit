package soulfit.soulfit.matching.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soulfit.soulfit.matching.ai.dto.AiMatchResultDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiApiResponseDto {
    private List<AiMatchResultDto> aiMatchResults;
}