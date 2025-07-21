package soulfit.soulfit.report.dto;

import lombok.*;
import soulfit.soulfit.report.domain.ReportReason;
import soulfit.soulfit.report.domain.ReportStatus;
import soulfit.soulfit.report.domain.ReportType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDto {
    private Long id;
    private ReportType targetType;
    private Long targetId;
    private String reportedByNickname;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime reportedAt;
    private LocalDateTime processedAt;
}
