package backend.auth.controller;

import backend.auth.security.TokenBlacklist;
import backend.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 관련 컨트롤러 (로그아웃, 토큰 갱신 등)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final TokenBlacklist tokenBlacklist;
    private final RefreshTokenService refreshTokenService;

    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader,
                                   Authentication authentication) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // 1. Access Token을 블랙리스트에 추가
                tokenBlacklist.addToBlacklist(token);
                
                // 2. Refresh Token 삭제
                if (authentication != null && authentication.getName() != null) {
                    refreshTokenService.deleteByUserId(authentication.getName());
                }
                
                log.info("로그아웃 성공: {}", authentication != null ? authentication.getName() : "unknown");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "로그아웃되었습니다."
                ));
            }
            
            return ResponseEntity.badRequest().body(Map.of(
                "error", "유효하지 않은 토큰입니다."
            ));
            
        } catch (Exception e) {
            log.error("로그아웃 처리 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "로그아웃 처리 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 토큰 유효성 검증
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", "토큰이 없습니다."
                ));
            }
            
            String token = authHeader.substring(7);
            
            // 블랙리스트 확인
            if (tokenBlacklist.isBlacklisted(token)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", "블랙리스트된 토큰입니다."
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "유효한 토큰입니다."
            ));
            
        } catch (Exception e) {
            log.error("토큰 검증 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "error", "토큰 검증 중 오류가 발생했습니다."
            ));
        }
    }
}