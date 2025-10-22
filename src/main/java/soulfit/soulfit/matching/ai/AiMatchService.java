package soulfit.soulfit.matching.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.matching.ai.dto.AiUserDto;
import soulfit.soulfit.valuestest.service.TestService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiMatchService {

    private final TestService testService;
    private final AiMatchClient aiMatchClient;

    public AiMatchResponseDto getAiRecommendedUsers(Long targetUserId, List<Long> candidateUserIds) {
        // 1. Fetch target user's values
        List<Integer> targetLifeValues = testService.getLifeValuesByUserId(targetUserId);
        List<Integer> targetLoveValues = testService.getLoveValuesByUserId(targetUserId);
        AiUserDto targetUserDto = new AiUserDto(targetUserId, targetLifeValues, targetLoveValues);

        // 2. Fetch candidate users' values
        List<AiUserDto> candidateUserDtos = candidateUserIds.stream()
                .map(candidateId -> {
                    List<Integer> candidateLifeValues = testService.getLifeValuesByUserId(candidateId);
                    List<Integer> candidateLoveValues = testService.getLoveValuesByUserId(candidateId);
                    return new AiUserDto(candidateId, candidateLifeValues, candidateLoveValues);
                })
                .collect(Collectors.toList());

        // 3. Construct AI server request DTO
        AiMatchRequestDto requestDto = new AiMatchRequestDto(targetUserDto, candidateUserDtos);

        // 4. Call AI server
        return aiMatchClient.match(requestDto);
    }
}