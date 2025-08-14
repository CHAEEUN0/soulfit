package soulfit.soulfit.matching.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VoteRequest {
    private Long voteFormId;
    private Long voteOptionId;

    // getters and setters
}
