package soulfit.soulfit.report.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.report.domain.Report;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByOrderByReportedAtDesc();
}

