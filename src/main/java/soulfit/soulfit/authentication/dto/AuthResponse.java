package soulfit.soulfit.authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String email;
    private String tokenType = "Bearer";
    private Long id;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, String username, String email,Long id) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.email = email;
        this.id = id;
    }
}
