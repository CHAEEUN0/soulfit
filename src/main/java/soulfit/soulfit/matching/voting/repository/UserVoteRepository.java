package soulfit.soulfit.matching.voting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.matching.voting.domain.UserVote;
import soulfit.soulfit.matching.voting.dto.VoteResultResponse;

import java.util.List;

@Repository
public interface UserVoteRepository extends JpaRepository<UserVote, Long> {
    boolean existsByVoterIdAndTargetUserIdAndVoteFormId(Long voterId, Long targetUserId, Long voteFormId);

    @Query("SELECT new soulfit.soulfit.matching.voting.dto.VoteResultResponse$OptionResult(vo.id, vo.label, COUNT(uv.id)) " +
           "FROM VoteOption vo LEFT JOIN UserVote uv ON uv.voteOption.id = vo.id AND uv.voteForm.id = :voteFormId " +
           "WHERE vo.voteForm.id = :voteFormId " +
           "GROUP BY vo.id, vo.label, vo.sortOrder " +
           "ORDER BY vo.sortOrder ASC")
    List<VoteResultResponse.OptionResult> getVoteResults(@Param("voteFormId") Long voteFormId);
}