package soulfit.soulfit.meeting.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SearchFilter {

    private String city;
    private String district;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minFee;
    private Integer maxFee;
    private Double minRating;
    private Double maxRating;
    private Integer minCapacity;
}
