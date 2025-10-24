package backend.auth.service;

import backend.auth.domain.RefreshToken;
import backend.auth.repository.RefreshTokenRepository;
import backend.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    
    /**
     * Refresh Token 생성 및 저장
     */
    @Transactional
    public String createOrUpdateRefreshToken(String userId) {
        String newToken = jwtUtil.generateRefreshToken(userId);
        LocalDateTime expiry = LocalDateTime.now()
                .plusSeconds(jwtUtil.getRefreshExpirationInSeconds());

        // 기존 토큰이 있으면 갱신, 없으면 새로 생성
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(existingToken -> {
                    existingToken.updateToken(newToken, expiry);
                    log.info("기존 Refresh Token 갱신: userId={}", userId);
                    return existingToken;
                })
                .orElseGet(() -> {
                    log.info("새 Refresh Token 생성: userId={}", userId);
                    return RefreshToken.builder()
                            .userId(userId)
                            .token(newToken)
                            .expiryDate(expiry)
                            .createdAt(LocalDateTime.now())
                            .build();
                });

        refreshTokenRepository.save(refreshToken);
        return newToken;
    }
    
    /**
     * Refresh Token 검증
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * Refresh Token 만료 여부 확인
     */
    public boolean verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            log.warn("만료된 Refresh Token 삭제: userId={}", token.getUserId());
            return false;
        }
        return true;
    }
    
    /**
     * Refresh Token 삭제 (로그아웃 시)
     */
    @Transactional
    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("Refresh Token 삭제: userId={}", userId);
    }
}
