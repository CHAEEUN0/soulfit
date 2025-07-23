package soulfit.soulfit.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.report.domain.Report;
import soulfit.soulfit.report.domain.ReportStatus;
import soulfit.soulfit.report.dto.ReportRequestDto;
import soulfit.soulfit.report.dto.ReportResponseDto;
import soulfit.soulfit.report.repository.ReportRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userAuthRepository;

    @Transactional
    public void createReport(ReportRequestDto requestDto, Long reporterId) {
        UserAuth reporter = userAuthRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자 정보가 존재하지 않습니다."));

        Report report = Report.builder()
                .targetType(requestDto.getTargetType())
                .targetId(requestDto.getTargetId())
                .reason(requestDto.getReason())
                .description(requestDto.getDescription())
                .reportedBy(reporter)
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReportsForAdmin() {
        return reportRepository.findAllByOrderByReportedAtDesc()
                .stream()
                .map(report -> ReportResponseDto.builder()
                        .id(report.getId())
                        .targetType(report.getTargetType())
                        .targetId(report.getTargetId())
                        .reportedByNickname(report.getReportedBy().getUsername())
                        .reason(report.getReason())
                        .description(report.getDescription())
                        .status(report.getStatus())
                        .reportedAt(report.getReportedAt())
                        .processedAt(report.getProcessedAt())
                        .build()
                )
                .toList();
    }
}
