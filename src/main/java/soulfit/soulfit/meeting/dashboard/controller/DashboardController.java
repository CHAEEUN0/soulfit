
package soulfit.soulfit.meeting.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.dashboard.dto.MeetingStatsResponseDto;
import soulfit.soulfit.meeting.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/meeting/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<MeetingStatsResponseDto> getMeetingStats(
            @AuthenticationPrincipal(expression = "id") Long userId) {
        MeetingStatsResponseDto stats = dashboardService.calculateMeetingStats(userId);
        return ResponseEntity.ok(stats);
    }
}
