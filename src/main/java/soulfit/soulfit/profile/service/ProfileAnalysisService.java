
package soulfit.soulfit.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.community.post.PostRepository;
import soulfit.soulfit.profile.client.AiProfileAnalysisClient;
import soulfit.soulfit.profile.domain.ProfileAnalysisReport;
import soulfit.soulfit.profile.dto.ai.ProfileAnalysisRequestDto;
import soulfit.soulfit.profile.dto.ai.ProfileAnalysisResponseDto;
import soulfit.soulfit.profile.repository.ProfileAnalysisReportRepository;
import soulfit.soulfit.report.repository.ReportRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileAnalysisService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final AiProfileAnalysisClient aiProfileAnalysisClient;
    private final ProfileAnalysisReportRepository profileAnalysisReportRepository;

    @Transactional
    public void analyzeAndProcessProfile(Long userId) {
        UserAuth user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 활동 데이터 조회 (게시글, 댓글, 피신고 횟수 등)
        int postCount = postRepository.countByPoster(user);
        // int commentCount = commentRepository.countByUser(user); // CommentRepository 필요
        int reportReceivedCount = reportRepository.countByTargetId(user.getId());

        ProfileAnalysisRequestDto requestDto = ProfileAnalysisRequestDto.builder()
                .userId(user.getId())
                .userNickname(user.getUsername())
                .profileIntroduction(user.getUserProfile().getBio()) // UserProfile 엔티티 필요
                .profileImageUrl(user.getUserProfile().getProfileImageUrl())       // UserProfile 엔티티 필요
                .postCount(postCount)
                // .commentCount(commentCount)
                .reportReceivedCount(reportReceivedCount)
                .createdAt(user.getCreatedAt())
                .build();

        try {
            ProfileAnalysisResponseDto response = aiProfileAnalysisClient.detectFakeProfile(requestDto);

            ProfileAnalysisReport report = new ProfileAnalysisReport(user, response.isFake(), response.getFakeScore(), response.getReasons());
            profileAnalysisReportRepository.save(report);

            if (response.isFake() && response.getFakeScore() > 0.8) {
                user.setAccountStatus(soulfit.soulfit.authentication.entity.AccountStatus.UNDER_REVIEW);
                userRepository.save(user);
                log.info("User {}'s account status set to UNDER_REVIEW due to high fake score.", user.getId());
            }

        } catch (Exception e) {
            log.error("Error while analyzing profile for user {}: {}", userId, e.getMessage());
            // AI 서버 통신 실패 시 예외 처리 (e.g., 관리자 알림)
        }
    }
}
