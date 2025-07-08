package soulfit.soulfit.meeting.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Embeddable
@NoArgsConstructor
public class Location {
    private String city;
    private String roadAddress;
    private String zipCode;

    double latitude;
    double longitude;

    @Builder
    public Location(String city, String roadAddress, String zipCode,
                    double latitude, double longitude) {
        this.city = city;
        this.roadAddress = roadAddress;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
