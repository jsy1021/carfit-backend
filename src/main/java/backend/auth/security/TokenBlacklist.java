package backend.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 토큰 블랙리스트 관리
 * 실제 운영환경에서는 Redis나 데이터베이스를 사용하는 것이 좋습니다.
 */
@Slf4j
@Component
public class TokenBlacklist {
    
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    /**
     * 토큰을 블랙리스트에 추가
     */
    public void addToBlacklist(String token) {
        blacklistedTokens.add(token);
        log.info("토큰이 블랙리스트에 추가됨: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
    
    /**
     * 블랙리스트에서 토큰 제거 (토큰 만료 시 정리용)
     */
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
        log.debug("토큰이 블랙리스트에서 제거됨");
    }
    
    /**
     * 블랙리스트 크기 반환 (모니터링용)
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}

