package soulfit.soulfit.authentication.dto;

import lombok.Builder;
import lombok.Data;
import soulfit.soulfit.authentication.entity.UserAuth;

@Data
@Builder
public class UserResponseDto {
    private Long id;
    private String username;

    public static UserResponseDto from(UserAuth user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
