package soulfit.soulfit.profile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class UserProfile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private UserAuth userAuth;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    public UserProfile() {}

    public UserProfile(UserAuth userAuth, LocalDate birthDate, Gender gender) {
        this.userAuth = userAuth;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
