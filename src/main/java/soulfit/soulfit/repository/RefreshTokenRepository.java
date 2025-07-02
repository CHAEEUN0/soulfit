package soulfit.soulfit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.entity.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUsername(String username);
    void deleteByUsername(String username);
    void deleteByToken(String token);
    List<RefreshToken> findAllByUsername(String username);
}
