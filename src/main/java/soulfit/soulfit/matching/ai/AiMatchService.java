package soulfit.soulfit.matching.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.ai.dto.AiMatchResultDto;
import soulfit.soulfit.matching.ai.dto.AiUserDto;
import soulfit.soulfit.matching.ai.dto.ClientMatchResultDto;
import soulfit.soulfit.valuestest.service.TestService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiMatchService {

    private final TestService testService;
    private final AiMatchClient aiMatchClient;
    private final UserRepository userRepository;

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
        AiMatchResponseDto aiResponse = aiMatchClient.match(requestDto);

        // 5. Enrich AI match results with username and profile image URL
        List<ClientMatchResultDto> enrichedResults = aiResponse.getAiMatchResults().stream()
                .map(aiMatchResultDto -> {
                    Optional<UserAuth> userAuthOptional = userRepository.findByIdWithProfile(aiMatchResultDto.getAiMatchResult().getUserId());
                    String username = userAuthOptional.map(UserAuth::getUsername).orElse("Unknown");
                    String profileImageUrl = userAuthOptional.map(UserAuth::getUserProfile)
                            .map(userProfile -> userProfile.getProfileImageUrl() != null ? userProfile.getProfileImageUrl() : "default_profile_image_url")
                            .orElse("default_profile_image_url");

                    return new ClientMatchResultDto(aiMatchResultDto.getAiMatchResult(), username, profileImageUrl);
                })
                .collect(Collectors.toList());

        return new AiMatchResponseDto(enrichedResults);
    }
}