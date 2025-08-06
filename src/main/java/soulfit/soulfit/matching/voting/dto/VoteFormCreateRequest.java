package soulfit.soulfit.matching.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class VoteFormCreateRequest {
    private String title;
    private String description;
    private List<String> options;
    private String targetType; // "PROFILE" 또는 "IMAGE"
    private String imageUrl;
}
