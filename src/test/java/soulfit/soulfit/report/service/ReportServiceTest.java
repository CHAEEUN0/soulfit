package soulfit.soulfit.report.service;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.report.domain.Report;
import soulfit.soulfit.report.domain.ReportReason;
import soulfit.soulfit.report.domain.ReportStatus;
import soulfit.soulfit.report.domain.ReportType;
import soulfit.soulfit.report.dto.ReportRequestDto;
import soulfit.soulfit.report.dto.ReportResponseDto;
import soulfit.soulfit.report.repository.ReportRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @MockitoBean
    private ReportRepository reportRepository;

    @MockitoBean
    private UserRepository userAuthRepository;


    @Autowired
    private EntityManager em;

    private UserAuth mockReporter;
    private UserAuth targetUser;

    @BeforeEach
    void setup() {
        mockReporter = new UserAuth();
        mockReporter.setId(5L);
        mockReporter.setUsername("testUser");
        mockReporter.setPassword("samplepwd");
        mockReporter.setEmail("testman@example.con");

        targetUser = new UserAuth();
        targetUser.setId(6L);
        targetUser.setUsername("target");
        targetUser.setPassword("targetPWD");
        targetUser.setEmail("target@example.con");
    }

    @Test
    void 사용자가_신고를_생성할_수_있다() {
        // given
        ReportRequestDto dto = ReportRequestDto.builder()
                .targetType(ReportType.USER)
                .targetId(2L)
                .reason(ReportReason.SPAM)
                .description("스팸 계정입니다")
                .build();
        when(userAuthRepository.findById(mockReporter.getId())).thenReturn(Optional.of(mockReporter));

        // when
        reportService.createReport(dto, mockReporter.getId());

        // then
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void 관리자_계정은_신고_목록을_조회할_수_있다() {
        // given
        Report mockReport = Report.builder()
                .id(100L)
                .targetType(ReportType.POST)
                .targetId(999L)
                .reportedBy(mockReporter)
                .reason(ReportReason.HARASSMENT)
                .description("욕설 포함")
                .status(ReportStatus.PENDING)
                .build();

        when(reportRepository.findAllByOrderByReportedAtDesc()).thenReturn(List.of(mockReport));

        // when
        List<ReportResponseDto> reports = reportService.getAllReportsForAdmin();

        // then
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getTargetType()).isEqualTo(ReportType.POST);
        assertThat(reports.get(0).getReason()).isEqualTo(ReportReason.HARASSMENT);
        assertThat(reports.get(0).getReportedByNickname()).isEqualTo("testUser");
    }
}