package soulfit.soulfit.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshTokenRequest {
    private String refreshToken;

    public RefreshTokenRequest() {}

}