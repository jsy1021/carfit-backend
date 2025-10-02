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
    public String createRefreshToken(String userId) {
        // 기존 Refresh Token 삭제
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(refreshTokenRepository::delete);
        
        // 새로운 Refresh Token 생성
        String token = jwtUtil.generateRefreshToken(userId);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationInSeconds()))
                .createdAt(LocalDateTime.now())
                .build();
        
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token 생성: userId={}", userId);
        
        return token;
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
