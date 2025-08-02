package soulfit.soulfit.meeting.dto.ai;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiRequestDto {
    private Long userId;
    private List<String> recentCategories;
    private List<String> recentKeywords;
    private List<String> bookmarkedCategories;
}
