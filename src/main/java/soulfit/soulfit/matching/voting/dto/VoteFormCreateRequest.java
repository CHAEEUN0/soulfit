package soulfit.soulfit.matching.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class VoteFormCreateRequest {
    private String title;
    private String description;
    private List<String> options;
}
