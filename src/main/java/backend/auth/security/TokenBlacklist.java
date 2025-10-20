package backend.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * JWT 토큰 블랙리스트 관리 (Redis 기반)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklist {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final long EXPIRATION_TIME_MINUTES = 15; // Access Token 만료시간과 동일
    
    /**
     * 토큰을 블랙리스트에 추가
     * TTL을 설정하여 15분 후 자동 만료
     */
    public void addToBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(EXPIRATION_TIME_MINUTES));
        log.info("토큰이 Redis 블랙리스트에 추가됨: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 블랙리스트에서 토큰 제거 (수동 정리용)
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("토큰이 Redis 블랙리스트에서 제거됨");
    }
    
    /**
     * 블랙리스트 크기 반환 (모니터링용)
     * Redis에서는 패턴 매칭으로 개수 계산
     */
    public long getBlacklistSize() {
        try {
            return redisTemplate.keys(BLACKLIST_PREFIX + "*").size();
        } catch (Exception e) {
            log.warn("블랙리스트 크기 조회 실패: {}", e.getMessage());
            return 0;
        }
    }
}