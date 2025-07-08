package soulfit.soulfit.meeting.dto;

import lombok.Builder;
import lombok.Data;
import soulfit.soulfit.meeting.domain.Category;

import java.time.LocalDate;

@Data
@Builder
public class MeetingFilter {


    private LocalDate startDate;
    private LocalDate endDate;
    private String city;
    private Category category;


    public MeetingFilter() {
    }

    public MeetingFilter(LocalDate startDate, LocalDate endDate, String city, Category category) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.city = city;
        this.category = category;
    }
}
