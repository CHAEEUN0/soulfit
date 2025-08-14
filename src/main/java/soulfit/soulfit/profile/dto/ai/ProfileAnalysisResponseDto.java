
package soulfit.soulfit.profile.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class ProfileAnalysisResponseDto {
    private Long userId;
    private boolean isFake;
    private double fakeScore;
    private List<String> reasons;
}
