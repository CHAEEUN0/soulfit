package soulfit.soulfit.valuestest.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ValuesTestAnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_session_id", nullable = false)
    private TestSession testSession;

    @Column(columnDefinition = "TEXT")
    private String analysisSummary;

    private String topValues; // List<String>을 직렬화하여 저장

    public ValuesTestAnalysisReport(TestSession testSession, String analysisSummary, String topValues) {
        this.testSession = testSession;
        this.analysisSummary = analysisSummary;
        this.topValues = topValues;
    }
}
