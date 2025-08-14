package soulfit.soulfit.valuestest.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ValuesTestAnalysisResponseDto {

    private String analysisSummary;
    private List<String> topValues;
    private Map<String, Double> scoreByFactor;
}
