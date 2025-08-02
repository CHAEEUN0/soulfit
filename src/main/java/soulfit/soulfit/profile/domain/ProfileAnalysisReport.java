
package soulfit.soulfit.profile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ProfileAnalysisReport {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAuth user;

    private boolean isFake;
    private double fakeScore;

    @ElementCollection
    private List<String> reasons;

    private LocalDateTime analyzedAt;

    public ProfileAnalysisReport(UserAuth user, boolean isFake, double fakeScore, List<String> reasons) {
        this.user = user;
        this.isFake = isFake;
        this.fakeScore = fakeScore;
        this.reasons = reasons;
        this.analyzedAt = LocalDateTime.now();
    }
}
