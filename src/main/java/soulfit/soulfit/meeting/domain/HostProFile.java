package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soulfit.soulfit.authentication.entity.UserAuth;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HostProFile {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private UserAuth userAuth;

    private double hostAverageRating;

    public HostProFile(UserAuth userAuth, double hostAverageRating) {
        this.userAuth = userAuth;
        this.hostAverageRating = hostAverageRating;
    }

    public void setHostAverageRating(double hostAverageRating) {
        this.hostAverageRating = hostAverageRating;
    }
}
