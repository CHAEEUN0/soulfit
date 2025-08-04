package soulfit.soulfit.matching.voting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.matching.voting.domain.VoteOption;

import java.util.List;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    List<VoteOption> findByVoteFormIdOrderBySortOrderAsc(Long voteFormId);
}
