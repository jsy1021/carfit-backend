package backend.auth.security;

import backend.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import backend.auth.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklist tokenBlacklist;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Authorization 헤더에서 JWT 토큰 추출
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. 토큰이 없거나 Bearer로 시작하지 않으면 다음 필터로
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " 제거하고 실제 토큰 추출
        jwt = authHeader.substring(7);
        
        // 3.5. 블랙리스트 확인
        if (tokenBlacklist.isBlacklisted(jwt)) {
            log.warn("블랙리스트된 토큰 사용 시도: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 4. 토큰에서 사용자명 추출
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // 토큰 파싱 실패 시 Refresh Token으로 자동 갱신 시도
            log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
            
            // Refresh Token으로 자동 갱신 시도
            String newAccessToken = attemptTokenRefresh(request);
            if (newAccessToken != null) {
                // 새로운 Access Token을 응답 헤더에 추가
                response.setHeader("X-New-Access-Token", newAccessToken);
                response.setHeader("X-Token-Refreshed", "true");
                log.info("JWT 토큰 자동 갱신 성공");
            }
            
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 사용자명이 있고 SecurityContext에 인증 정보가 없으면
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 6. 사용자 정보 로드
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            
            // 7. 토큰 유효성 검증
            if (jwtUtil.validateToken(jwt, userDetails)) {
                
                // 8. 인증 토큰 생성
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                // 9. 요청 세부 정보 설정
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 10. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT 인증 성공: username={}", username);
            } else {
                log.warn("JWT 토큰 유효성 검증 실패: username={}", username);
            }
        }

        // 11. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
    
    /**
     * Refresh Token을 사용한 자동 토큰 갱신 시도
     */
    private String attemptTokenRefresh(HttpServletRequest request) {
        try {
            // Cookie에서 Refresh Token 추출
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            
            String refreshToken = null;
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
            
            if (refreshToken == null) {
                log.debug("Refresh Token이 없습니다.");
                return null;
            }
            
            // Refresh Token 검증
            Optional<backend.auth.domain.RefreshToken> storedToken = refreshTokenService.findByToken(refreshToken);
            if (storedToken.isEmpty()) {
                log.debug("유효하지 않은 Refresh Token입니다.");
                return null;
            }
            
            // 만료 확인
            if (!refreshTokenService.verifyExpiration(storedToken.get())) {
                log.debug("Refresh Token이 만료되었습니다.");
                return null;
            }
            
            // 사용자 정보 로드
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(storedToken.get().getUserId());
            
            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            
            log.info("자동 토큰 갱신 성공: userId={}", storedToken.get().getUserId());
            return newAccessToken;
            
        } catch (Exception e) {
            log.warn("자동 토큰 갱신 실패: {}", e.getMessage());
            return null;
        }
    }
}
