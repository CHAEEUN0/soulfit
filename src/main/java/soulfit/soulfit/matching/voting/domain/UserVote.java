package soulfit.soulfit.matching.voting.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "user_vote",
        uniqueConstraints = @UniqueConstraint(columnNames = {"voter_id", "target_user_id", "vote_form_id"})
)
public class UserVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 투표자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private UserAuth voter;

    // 투표 대상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private UserAuth targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_form_id", nullable = false)
    private VoteForm voteForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_option_id", nullable = false)
    private VoteOption voteOption;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean deleted = false;

    public static UserVote createVote(UserAuth voter, UserAuth targetUser, VoteForm voteForm, VoteOption voteOption) {
        UserVote vote = new UserVote();
        vote.voter = voter;
        vote.targetUser = targetUser;
        vote.voteForm = voteForm;
        vote.voteOption = voteOption;
        vote.createdAt = LocalDateTime.now();
        vote.deleted = false;
        return vote;
    }
}

