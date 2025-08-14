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
    private String targetType;
    private String imageUrl;
    private List<VoteOptionResponse> options;

    public VoteFormResponse(Long id, String title, Long creatorId, String creatorUsername, String creatorProfileImageUrl, String targetType, String imageUrl, List<VoteOptionResponse> options) {
        this.id = id;
        this.title = title;
        this.creatorId = creatorId;
        this.creatorUsername = creatorUsername;
        this.creatorProfileImageUrl = creatorProfileImageUrl;
        this.targetType = targetType;
        this.imageUrl = imageUrl;
        this.options = options;
    }

    // getters
}