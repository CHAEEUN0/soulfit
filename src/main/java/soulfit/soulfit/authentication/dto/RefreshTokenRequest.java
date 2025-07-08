package soulfit.soulfit.authentication.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshTokenRequest {
    private String refreshToken;

    public RefreshTokenRequest() {}

}