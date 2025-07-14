package soulfit.soulfit.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StartTestSessionResponse {
    private Long sessionId;
    private List<QuestionResponse> questions;
}
