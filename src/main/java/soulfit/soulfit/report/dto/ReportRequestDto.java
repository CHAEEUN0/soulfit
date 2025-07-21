package soulfit.soulfit.report.dto;
import lombok.*;
import soulfit.soulfit.report.domain.ReportReason;
import soulfit.soulfit.report.domain.ReportType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDto {
    private ReportType targetType;
    private Long targetId;
    private ReportReason reason;
    private String description;
}

