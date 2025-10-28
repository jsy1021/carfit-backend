package backend.user.controller;

import backend.common.util.AESUtil;
import backend.user.dto.LoginResponseDto;
import backend.user.service.NaverAuthService;
import backend.auth.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth/naver")
@Slf4j
public class NaverLoginController {

    private final NaverAuthService naverAuthService;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public ResponseEntity<?> naverLogin(@RequestParam("code") String code,
                                        HttpServletResponse response) {
        try {
            log.info("네이버 로그인 요청 - code: {}", code);

            // 1. 네이버 인증 처리 및 JWT 토큰 발급
            Map<String, Object> naverResult = naverAuthService.processNaverLogin(code);

            // 2. NaverAuthService에서 반환된 사용자 정보 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = (Map<String, Object>) naverResult.get("user");
            
            String accessToken = (String) naverResult.get("accessToken");
            String refreshToken = (String) naverResult.get("refreshToken");
            boolean isNewUser = (Boolean) naverResult.get("isNewUser");

            // 3. Refresh Token을 httpOnly Cookie로 설정
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            response.addCookie(refreshTokenCookie);

            // 4. LoginResponseDto로 응답 구성
            LoginResponseDto loginResponse = LoginResponseDto.builder()
                    .success(true)
                    .message(isNewUser ? "네이버 로그인 성공 (신규 사용자)" : "네이버 로그인 성공")
                    .token(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessExpirationInSeconds())
                    .user(LoginResponseDto.UserInfo.builder()
                            .userId((String) userInfo.get("userId"))
                            .name((String) userInfo.get("name"))
                            .email((String) userInfo.get("email"))
                            .address((String) userInfo.get("address"))
                            .role((String) userInfo.get("role"))
                            .profileImageUrl((String) userInfo.get("profileImageUrl"))
                            .build())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("네이버 로그인 성공 - 사용자: {}, isNewUser: {}", userInfo.get("userId"), isNewUser);
            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            log.error("네이버 로그인 실패", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDto.builder()
                            .success(false)
                            .message("네이버 로그인 실패: " + e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
}
