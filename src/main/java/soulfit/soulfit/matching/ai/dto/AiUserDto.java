package soulfit.soulfit.matching.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiUserDto {
    private Long userId;
    private List<Integer> lifeValues;
    private List<Integer> loveValues;
}
