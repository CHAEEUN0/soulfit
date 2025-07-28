package soulfit.soulfit.meeting.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Embeddable
@NoArgsConstructor
public class Location {
    private String city; // 시도(sido)
    private String district; // 시군구(sigungu)
    private String address; // 도로명 주소(address)
    private String detailAddress; //상세주소만 직접 입력
    private String zipCode; //우편번호(zonecode)

    double latitude;
    double longitude;

    @Builder
    public Location(String city, String district, String address, String detailAddress, String zipCode, double latitude, double longitude) {
        this.city = city;
        this.district = district;
        this.address = address;
        this.detailAddress = detailAddress;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
