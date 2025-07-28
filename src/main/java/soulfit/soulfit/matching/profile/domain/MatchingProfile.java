package soulfit.soulfit.matching.profile.domain;
import jakarta.persistence.*;
import lombok.*;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAuth userAuth;

    private String bio;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    private String job;

    private Integer heightCm;
    private Integer weightKg;

    @Enumerated(EnumType.STRING)
    private Religion religion;

    @Enumerated(EnumType.STRING)
    private SmokingHabit smoking;

    @Enumerated(EnumType.STRING)
    private DrinkingHabit drinking;

    @ManyToMany
    @JoinTable(
            name = "matching_profile_ideal_type",
            joinColumns = @JoinColumn(name = "matching_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "ideal_type_keyword_id")
    )
    @Builder.Default
    private Set<IdealTypeKeyword> idealTypes = new HashSet<>();
}
