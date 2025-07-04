package soulfit.soulfit.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.meeting.domain.Category;
import soulfit.soulfit.meeting.domain.Meeting;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByTitleContainingIgnoreCase(String keyword);


    @Query("""
        SELECT m FROM Meeting m
        WHERE (:category IS NULL OR m.category = :category)
          AND (:city IS NULL OR m.location.city = :city)
          AND m.meetingTime BETWEEN :start AND :end
    """)
    List<Meeting> filterMeetings(
            @Param("category") Category category,
            @Param("city") String city,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

}
