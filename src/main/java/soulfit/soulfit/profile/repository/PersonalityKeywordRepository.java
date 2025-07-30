package soulfit.soulfit.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.profile.domain.PersonalityKeyword;
import soulfit.soulfit.profile.domain.UserProfile;

public interface PersonalityKeywordRepository extends JpaRepository<PersonalityKeyword, Long> {
    void deleteAllByUserProfile(UserProfile userProfile);
}