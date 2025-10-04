package soulfit.soulfit.matching.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.matching.profile.domain.MatchingProfile;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.util.Optional;

// MatchingProfileRepository.java
public interface MatchingProfileRepository extends JpaRepository<MatchingProfile, Long> {
    Optional<MatchingProfile> findByUserAuthId(Long userId);
    Optional<MatchingProfile> findByUserAuth(UserAuth userAuth);
}

