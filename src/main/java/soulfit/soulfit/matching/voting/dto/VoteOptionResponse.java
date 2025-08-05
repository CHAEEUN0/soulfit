package soulfit.soulfit.matching.voting.dto;

import lombok.Getter;

@Getter
public class VoteOptionResponse {
    private Long id;
    private String label;
    private String emoji;

    public VoteOptionResponse(Long id, String label, String emoji) {
        this.id = id;
        this.label = label;
        this.emoji = emoji;
    }

}
