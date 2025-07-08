package soulfit.soulfit.test.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "test_session")
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAuth user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TestType testType; // TypeA, TypeB 등

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status; // enum: IN_PROGRESS, COMPLETED

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestAnswer> answers = new ArrayList<>();
}
