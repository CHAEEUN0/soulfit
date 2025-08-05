package soulfit.soulfit.valuestest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.valuestest.domain.ValuesTestAnalysisReport;

import java.util.Optional;

public interface ValuesTestAnalysisReportRepository extends JpaRepository<ValuesTestAnalysisReport, Long> {
    Optional<ValuesTestAnalysisReport> findByTestSessionId(Long testSessionId);
}
