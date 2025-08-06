package soulfit.soulfit.valuestest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import soulfit.soulfit.valuestest.domain.TestType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class UserTestResult {
    private Long sessionId;
    private TestType testType;
    private LocalDateTime submittedAt;
    private List<TestAnswerResponse> answers;
    private AIResult aiResult;

    @Data
    @AllArgsConstructor
    public static class AIResult {
        private String summary;
        private List<String> topValues;
    }
}

