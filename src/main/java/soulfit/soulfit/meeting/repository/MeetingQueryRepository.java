package soulfit.soulfit.meeting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.dto.SearchFilter;

public interface MeetingQueryRepository {

    Page<Meeting> search(SearchFilter searchFilter, Pageable pageable);
}
