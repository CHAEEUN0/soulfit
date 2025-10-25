package soulfit.soulfit.meeting.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ParticipantStatsDto {
    private double malePercent;
    private double femalePercent;
    private Map<String, Double> ageGroupDistribution;
}
