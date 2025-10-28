package backend.user.controller;


import backend.auth.service.RefreshTokenService;
import backend.common.util.AESUtil;
import backend.user.dto.*;
import backend.auth.security.JwtUtil;
import backend.auth.security.TokenBlacklist;
import backend.auth.service.AuthMailService;

import backend.user.service.UserService;
import backend.auth.dto.*;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist tokenBlacklist;
    private final RefreshTokenService refreshTokenService;
    private final AuthMailService authMailService;
    private final backend.user.service.S3Service s3Service;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequestDto,
                                   HttpServletResponse response) {
        log.info("로그인 시도: userId={}", loginRequestDto.getUserId());
        
        try {
            // 1. UserService로 사용자 검증 (비밀번호 확인 등)
            userService.login(loginRequestDto);

            // 2. 인증 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUserId(),
                            loginRequestDto.getPassword()
                    );

            // 3. AuthenticationManager로 인증 시도
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 4. 사용자 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.getUserId());

            // 5. Access Token 생성 (15분)
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            
            // 6. Refresh Token 생성 및 저장 (7일)
            String refreshToken = refreshTokenService.createRefreshToken(loginRequestDto.getUserId());
            
            // 7. Refresh Token을 httpOnly Cookie로 설정
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true); // XSS 공격 방어
            refreshTokenCookie.setSecure(false); // HTTPS에서만 전송 (개발 환경에서는 false)
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            response.addCookie(refreshTokenCookie);

            // 8. 사용자 정보 조회 (응답용)
            backend.user.domain.User user = ((backend.auth.security.CustomUserDetails) userDetails).getUser();

            // 9. 응답 데이터 구성
            LoginResponseDto loginResponse = LoginResponseDto.builder()
                    .success(true)
                    .message("로그인 성공")
                    .token(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessExpirationInSeconds())
                    .user(LoginResponseDto.UserInfo.builder()
                            .userId(user.getUserId())
                            .name(user.getName())
                            .email(AESUtil.decrypt(user.getEmail()))
                            .address(AESUtil.decrypt(user.getAddress()))
                            .role(user.getRole())
                            .profileImageUrl(user.getProfileImageUrl())
                            .build())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("로그인 성공: userId={}", loginRequestDto.getUserId());
            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            log.warn("로그인 실패: userId={}, error={}", loginRequestDto.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패: " + e.getMessage());
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        log.info("회원가입 시도: userId={}, email={}", signupRequestDto.getUserId(), signupRequestDto.getEmail());
        
        try {
            userService.registerUser(signupRequestDto);
            log.info("회원가입 성공: userId={}", signupRequestDto.getUserId());
            return ResponseEntity.ok("Signup successful");
        }
        catch (Exception e) {
            log.warn("회원가입 실패: userId={}, error={}", signupRequestDto.getUserId(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                         HttpServletResponse response) {
        log.info("토큰 갱신 시도");
        
        try {
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh Token이 없습니다."));
            }
            
            // 1. Refresh Token 검증
            backend.auth.domain.RefreshToken storedToken = refreshTokenService.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token입니다."));
            
            // 2. 만료 확인
            if (!refreshTokenService.verifyExpiration(storedToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh Token이 만료되었습니다."));
            }
            
            // 3. 사용자 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(storedToken.getUserId());
            
            // 4. 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            
            // 5. 응답 구성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "토큰 갱신 성공");
            responseData.put("token", newAccessToken);
            responseData.put("tokenType", "Bearer");
            responseData.put("expiresIn", jwtUtil.getAccessExpirationInSeconds());
            
            log.info("토큰 갱신 성공: userId={}", storedToken.getUserId());
            return ResponseEntity.ok(responseData);
            
        } catch (Exception e) {
            log.warn("토큰 갱신 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰 갱신 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        try {
            // 1. Authorization 헤더에서 Access Token 추출하여 블랙리스트에 추가
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenBlacklist.addToBlacklist(token);
                log.info("Access Token이 블랙리스트에 추가됨 (로그아웃)");
            }
            
            // 2. Refresh Token 삭제
            if (refreshToken != null) {
                refreshTokenService.findByToken(refreshToken).ifPresent(token -> {
                    refreshTokenService.deleteByUserId(token.getUserId());
                    log.info("Refresh Token 삭제: userId={}", token.getUserId());
                });
            }
            
            // 3. Refresh Token 쿠키 삭제
            Cookie cookie = new Cookie("refreshToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(0); // 즉시 삭제
            response.addCookie(cookie);
            
            Map<String, String> responseData = new HashMap<>();
            responseData.put("message", "로그아웃 성공");
            return ResponseEntity.ok(responseData);
            
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "로그아웃 실패"));
        }
    }


    /**
     * 아이디 중복 체크
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicateUserId(@RequestParam("userId") String userId) {
        log.info("아이디 중복 체크 요청: userId={}", userId);
        
        try {
            boolean isDuplicate = userService.existsByUserId(userId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("isDuplicate", isDuplicate);
            responseData.put("message", isDuplicate ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.");
            
            log.info("아이디 중복 체크 결과: userId={}, isDuplicate={}", userId, isDuplicate);
            return ResponseEntity.ok(responseData);
            
        } catch (Exception e) {
            log.error("아이디 중복 체크 중 오류: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "중복 체크 실패"));
        }
    }

    /**
     * 이메일 인증번호 발송
     */
    @PostMapping("/send-auth-email")
    public ResponseEntity<?> sendAuthEmail(@Valid @RequestBody SendAuthEmailRequest request) {
        log.info("이메일 인증번호 발송 요청: email={}", request.getEmail());
        
        try {
            // 이메일을 해시하여 고유 키 생성
            Long key = (long) request.getEmail().hashCode();
            
            AuthNumberResponse response = authMailService.sendCodeEmail(request.getEmail(), key);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", response.getMessage());
            responseData.put("key", key); // 프론트엔드에서 검증 시 필요
            
            log.info("인증번호 발송 성공: email={}", request.getEmail());
            return ResponseEntity.ok(responseData);
            
        } catch (Exception e) {
            log.error("인증번호 발송 실패: email={}, error={}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "인증번호 발송 실패: " + e.getMessage()));
        }
    }

    /**
     * 이메일 인증번호 검증
     */
    @PostMapping("/verify-auth-number")
    public ResponseEntity<?> verifyAuthNumber(@Valid @RequestBody VerifyAuthNumberRequest request) {
        log.info("인증번호 검증 요청: email={}, authNumber={}", request.getEmail(), request.getAuthNumber());
        
        try {
            // 이메일을 해시하여 키 생성 (발송 시와 동일한 방식)
            Long key = (long) request.getEmail().hashCode();
            
            boolean isValid = authMailService.verifyAuthNumber(key, request.getAuthNumber());
            
            if (isValid) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "인증 성공");
                
                log.info("인증번호 검증 성공: email={}", request.getEmail());
                return ResponseEntity.ok(responseData);
            } else {
                log.warn("인증번호 검증 실패: email={}", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "인증번호가 일치하지 않거나 만료되었습니다."));
            }
            
        } catch (Exception e) {
            log.error("인증번호 검증 중 오류: email={}, error={}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "인증 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/profile/image/upload")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // S3에 실제 파일 업로드
            String folder = "profile/" + userDetails.getUsername() + "/";
            String s3Url = s3Service.uploadFile(file, folder);
            
            log.info("S3 업로드 완료: s3Url={}", s3Url);
            
            // 사용자 프로필 이미지 업데이트
            userService.updateProfileImage(userDetails.getUsername(), s3Url);
            
            log.info("프로필 이미지 업로드 성공: userId={}, s3Url={}", userDetails.getUsername(), s3Url);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로필 이미지가 업로드되었습니다.",
                    "profileImageUrl", s3Url
            ));
            
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패: userId={}, error={}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "이미지 업로드 중 오류가 발생했습니다."));
        }
    }

}

