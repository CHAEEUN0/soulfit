package soulfit.soulfit.meeting.domain;

import jakarta.persistence.Embeddable;


@Embeddable
public class Address {
    private String city;
    private String roadAddress;
    private String zipCode;
}
