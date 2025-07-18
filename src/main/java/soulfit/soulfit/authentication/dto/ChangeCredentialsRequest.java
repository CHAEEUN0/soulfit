package soulfit.soulfit.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// ChangeCredentialsRequest.java
@Getter
@Setter
public class ChangeCredentialsRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank
    private String accessToken;

    private String newPassword; // 선택사항
}
