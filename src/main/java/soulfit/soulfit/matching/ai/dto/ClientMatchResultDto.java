package soulfit.soulfit.matching.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientMatchResultDto {
    private AiMatchResultDto aiMatchResult;
    private String username;
    private String profileImageUrl;
}