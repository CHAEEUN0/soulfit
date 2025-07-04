package soulfit.soulfit.authentication.service;


import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.config.JwtProperties;
import soulfit.soulfit.authentication.entity.RefreshToken;
import soulfit.soulfit.authentication.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtProperties jwtProperties;

    private final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    public RefreshToken createRefreshToken(String username) {
        // 기존 refresh token 삭제 (한 사용자당 하나의 refresh token만 유지)
        refreshTokenRepository.deleteByUsername(username);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 7일 유효

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    public void deleteAllByUsername(String username) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUsername(username);
        refreshTokenRepository.deleteAll(tokens);
    }
}
