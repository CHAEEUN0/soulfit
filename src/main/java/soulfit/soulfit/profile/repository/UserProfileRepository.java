package soulfit.soulfit.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.profile.domain.UserProfile;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserAuthId(Long userAuthId);
    Optional<UserProfile> findByUserAuth(soulfit.soulfit.authentication.entity.UserAuth userAuth);
}
