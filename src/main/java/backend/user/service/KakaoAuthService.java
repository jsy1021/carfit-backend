package backend.user.service;

import backend.auth.security.CustomUserDetails;
import backend.auth.security.JwtUtil;
import backend.auth.service.RefreshTokenService;
import backend.common.util.AESUtil;
import backend.user.util.KakaoUtil;
import backend.user.domain.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 카카오 OAuth 로그인 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * 카카오 인증 코드로 로그인 처리
     * @param code 카카오 인증 코드
     * @param clientId 카카오 앱 키
     * @param redirectUri 리다이렉트 URI
     * @param clientSecret 카카오 앱 시크릿
     * @return JWT 토큰과 사용자 정보
     */
    @Transactional
    public Map<String, Object> processKakaoLogin(String code) {
        try {
            // 1. 카카오 액세스 토큰 획득
            String kakaoAccessToken = kakaoUtil.getAccessToken(code);
            log.info("카카오 액세스 토큰 획득 성공");

            // 2. 카카오 사용자 정보 조회
            Map<String, Object> kakaoUserInfo = kakaoUtil.getUserInfo(kakaoAccessToken);
            String socialId = (String) kakaoUserInfo.get("id");
            String nickname = (String) kakaoUserInfo.get("nickname");
            String email=(String) kakaoUserInfo.get("email");
            log.info(email);
            String profileImageUrl = (String) kakaoUserInfo.get("profile_image_url");

            log.info("카카오 사용자 정보 - ID: {}, 닉네임: {}, 이메일: {}", socialId, nickname, email);

            // 3. DB에서 사용자 조회 또는 생성
            User user = findOrCreateUser(socialId, nickname, email, profileImageUrl);

            // 4. 백엔드 JWT 토큰 생성
            CustomUserDetails userDetails = new CustomUserDetails(user);
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = refreshTokenService.createOrUpdateRefreshToken(user.getUserId());

            // 5. 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            // 주소 정보 처리 (암호화된 주소 복호화)
            String decryptedAddress = null;
            if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                try {
                    decryptedAddress = AESUtil.decrypt(user.getAddress());
                } catch (Exception e) {
                    log.warn("주소 복호화 실패: {}", e.getMessage());
                }
            }

            response.put("user", Map.of(
                "userId", user.getUserId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "address", decryptedAddress,
                "profileImageUrl", user.getProfileImageUrl(),
                "role", user.getRole()
            ));
            response.put("isNewUser", user.getCreatedAt().getTime() > System.currentTimeMillis() - 5000); // 5초 이내 생성된 사용자

            log.info("카카오 로그인 성공 - 사용자: {}", user.getUserId());
            return response;

        } catch (Exception e) {
            log.error("카카오 로그인 처리 실패", e);
            throw new RuntimeException("카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 조회 또는 생성
     */
    private User findOrCreateUser(String socialId, String nickname, String email, String profileImageUrl) {
        // 1. 소셜 ID로 기존 사용자 조회
        Optional<User> existingUser = userRepository.findBySocialIdAndProvider(socialId, "kakao");
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // 기존 사용자 정보 업데이트 (닉네임, 이메일, 프로필 이미지가 변경될 수 있음)
            user.updateSocialUserInfo(nickname, email, profileImageUrl);
            userRepository.save(user);
            log.info("기존 카카오 사용자 로그인: {}", user.getUserId());
            return user;
        }

        // 2. 새 사용자 생성
        User newUser = new User(socialId, "kakao", nickname, email, profileImageUrl);
        userRepository.save(newUser);
        log.info("새 카카오 사용자 생성: {}", newUser.getUserId());
        return newUser;
    }

    /**
     * 카카오 사용자 정보로부터 이메일 추출 (카카오 API 응답 구조에 따라)
     */
    private String extractEmailFromKakaoInfo(Map<String, Object> userInfo) {
        // 카카오 API 응답에서 이메일 정보 추출
        // 실제 카카오 API 응답 구조에 맞게 수정 필요
        return (String) userInfo.get("email");
    }
}
