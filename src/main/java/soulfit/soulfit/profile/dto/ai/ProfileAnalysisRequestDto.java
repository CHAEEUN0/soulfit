
package soulfit.soulfit.profile.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ProfileAnalysisRequestDto {
    private Long userId;
    private String userNickname;
    private String profileIntroduction;
    private String profileImageUrl;
    private int postCount;
    private int commentCount;
    private int reportReceivedCount;
    private LocalDateTime createdAt;
}
