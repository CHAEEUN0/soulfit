package soulfit.soulfit.authentication.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
public class TokenBlacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime blacklistedAt;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // 기본 생성자
    public TokenBlacklist() {}

    public TokenBlacklist(String token, String username, LocalDateTime expiryDate) {
        this.token = token;
        this.username = username;
        this.blacklistedAt = LocalDateTime.now();
        this.expiryDate = expiryDate;
    }
}