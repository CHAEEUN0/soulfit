package soulfit.soulfit.meeting.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.meeting.domain.Meeting;
import java.util.List;
import soulfit.soulfit.authentication.entity.UserAuth;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long>, MeetingQueryRepository {

    List<Meeting> findByTitleContainingIgnoreCase(String keyword);

    List<Meeting> findByHost(UserAuth host);

    List<Meeting> findByTitle(String title);
}
