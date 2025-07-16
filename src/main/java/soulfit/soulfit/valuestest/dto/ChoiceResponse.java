package soulfit.soulfit.valuestest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChoiceResponse {
    private Long choiceId;
    private String content;
}
