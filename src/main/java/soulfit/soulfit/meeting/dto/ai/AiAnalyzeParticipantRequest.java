package soulfit.soulfit.meeting.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalyzeParticipantRequest {

    /**
     * 모임 ID
     */
    private Long meetingId;

    /**
     * 성별별 참여자 수 (e.g., {"MALE": 3, "FEMALE": 3})
     */
    private Map<String, Integer> genderCounts;

    /**
     * 연령층별 참여자 수 (e.g., {"TWENTIES": 4, "THIRTIES": 2})
     */
    private Map<String, Integer> ageBandCounts;
}