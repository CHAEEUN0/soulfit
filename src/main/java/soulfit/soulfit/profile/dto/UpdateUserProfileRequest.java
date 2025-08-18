package soulfit.soulfit.profile.dto;

import lombok.Data;
import soulfit.soulfit.profile.domain.MbtiType;

import java.util.List;

@Data
public class UpdateUserProfileRequest {

    private String bio;
    private MbtiType mbti;
    private List<String> personalityKeywords;
    private String region;
    private Double latitude;
    private Double longitude;
}
