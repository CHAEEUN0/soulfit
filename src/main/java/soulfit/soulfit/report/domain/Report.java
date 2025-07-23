package soulfit.soulfit.report.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고 대상 유형 (USER, MEETING, POST 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType targetType;

    // 신고 대상의 ID
    @Column(nullable = false)
    private Long targetId;

    // 신고자 (UserAuth 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private UserAuth reportedBy;

    // 신고 사유 (ENUM)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    // 상세 설명 (선택 입력)
    @Column(columnDefinition = "TEXT")
    private String description;

    // 신고 시간 (자동 생성)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    // 처리 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    // 처리 시간
    private LocalDateTime processedAt;

    // 신고 처리 시 상태와 처리 시간 설정
    public void process(ReportStatus status) {
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
}
