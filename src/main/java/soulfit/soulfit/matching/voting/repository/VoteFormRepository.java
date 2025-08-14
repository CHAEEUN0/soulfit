package soulfit.soulfit.matching.voting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.matching.voting.domain.VoteForm;

import java.util.Optional;

@Repository
public interface VoteFormRepository extends JpaRepository<VoteForm, Long> {
    Optional<VoteForm> findByIdAndActiveTrue(Long id);
}