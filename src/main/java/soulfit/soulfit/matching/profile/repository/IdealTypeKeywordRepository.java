package soulfit.soulfit.matching.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.matching.profile.domain.IdealTypeKeyword;

import java.util.Optional;

// IdealTypeKeywordRepository.java
public interface IdealTypeKeywordRepository extends JpaRepository<IdealTypeKeyword, Long> {
    Optional<IdealTypeKeyword> findByKeyword(String keyword);
}

