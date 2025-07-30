package soulfit.soulfit.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.meeting.domain.HostProFile;

@Repository
public interface HostProfileRepository extends JpaRepository<HostProFile, Long> {

}
