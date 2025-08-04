package soulfit.soulfit.matching.voting.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class VoteFormResponse {
    private Long id;
    private String title;
    private Long creatorId;
    private String creatorUsername;
    private String creatorProfileImageUrl;
    private List<VoteOptionResponse> options;

    public VoteFormResponse(Long id, String title, Long creatorId, String creatorUsername, String creatorProfileImageUrl, List<VoteOptionResponse> options) {
        this.id = id;
        this.title = title;
        this.creatorId = creatorId;
        this.creatorUsername = creatorUsername;
        this.creatorProfileImageUrl = creatorProfileImageUrl;
        this.options = options;
    }

    // getters
}