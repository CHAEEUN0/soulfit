package soulfit.soulfit.service;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import soulfit.soulfit.entity.TokenBlacklist;
import soulfit.soulfit.repository.TokenBlacklistRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
@Service
@Transactional
public class TokenBlacklistService {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    // JwtUtil 의존성 제거 - 파라미터로 필요한 정보를 받도록 수정
    public void addToBlacklist(String token, String username, Date expiration) {
        try {
            LocalDateTime expiryDate = expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            TokenBlacklist blacklistedToken = new TokenBlacklist(token, username, expiryDate);
            tokenBlacklistRepository.save(blacklistedToken);
        } catch (Exception e) {
            // 토큰이 이미 만료되었거나 유효하지 않은 경우 무시
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    // 만료된 블랙리스트 토큰 정리 (스케줄러에서 호출)
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void cleanupExpiredTokens() {
        tokenBlacklistRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}