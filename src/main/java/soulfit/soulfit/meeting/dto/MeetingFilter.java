package soulfit.soulfit.meeting.dto;

import lombok.Data;
import soulfit.soulfit.meeting.domain.Category;

import java.time.LocalDate;

@Data
public class MeetingFilter {
    private LocalDate startDate;
    private LocalDate endDate;
    private String city;
    private Category category;
}
