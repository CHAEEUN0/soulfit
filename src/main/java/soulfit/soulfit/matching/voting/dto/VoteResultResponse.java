package soulfit.soulfit.matching.voting.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class VoteResultResponse {
    private Long voteFormId;
    private String title;
    private List<OptionResult> results;

    public VoteResultResponse(Long voteFormId, String title, List<OptionResult> results) {
        this.voteFormId = voteFormId;
        this.title = title;
        this.results = results;
    }

    @Getter
    public static class OptionResult {
        private Long optionId;
        private String label;
        private long voteCount;

        public OptionResult(Long optionId, String label, long voteCount) {
            this.optionId = optionId;
            this.label = label;
            this.voteCount = voteCount;
        }
    }
}
