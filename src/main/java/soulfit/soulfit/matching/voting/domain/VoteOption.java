package soulfit.soulfit.matching.voting.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vote_option")
public class VoteOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_form_id", nullable = false)
    private VoteForm voteForm;

    @Column(nullable = false)
    private String label;

    private String emoji;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // Getters, setters, constructor(생략 가능)
}
