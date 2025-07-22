package soulfit.soulfit.report.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.report.dto.ReportRequestDto;
import soulfit.soulfit.report.dto.ReportResponseDto;
import soulfit.soulfit.report.service.ReportService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    // 일반 사용자 신고
    @PostMapping
    public void report(
            @RequestBody ReportRequestDto requestDto,
            @AuthenticationPrincipal UserAuth reporter // JWT 인증 기반
    ) {
        reportService.createReport(requestDto, reporter.getId());
    }

    // 관리자만 신고 목록 조회 가능
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReportResponseDto> getAllReports() {
        return reportService.getAllReportsForAdmin();
    }
}

