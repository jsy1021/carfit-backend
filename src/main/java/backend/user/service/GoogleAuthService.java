package backend.user.service;

import backend.auth.security.CustomUserDetails;
import backend.auth.security.JwtUtil;
import backend.auth.service.RefreshTokenService;
import backend.common.util.AESUtil;
import backend.user.domain.User;
import backend.user.repository.UserRepository;
import backend.user.util.GoogleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    private final GoogleUtil googleOAuthUtil;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${aws.s3.default-profile-url}")
    private String defaultProfileUrl;

    @Transactional
    public Map<String, Object> processGoogleLogin(String code) {
        log.info("[GoogleAuthService] 구글 로그인 시작 - code: {}", code);

        // 1. 구글 액세스 토큰 발급
        String accessToken = googleOAuthUtil.getAccessToken(code);
        log.info("구글 Access Token 발급 완료: {}", accessToken != null ? "OK" : "NULL");

        // 2. 사용자 정보 조회
        Map<String, Object> googleUserInfo = googleOAuthUtil.getUserInfo(accessToken);
        log.info("구글 사용자 정보 원본: {}", googleUserInfo);

        if (googleUserInfo == null || googleUserInfo.isEmpty()) {
            throw new IllegalStateException("구글 사용자 정보를 불러오지 못했습니다.");
        }

        String providerId = (String) googleUserInfo.get("id");
        String name = (String) googleUserInfo.get("name");
        String email = googleUserInfo.get("email") != null ? (String) googleUserInfo.get("email") : "";


        log.info("구글 사용자 정보 확인 - providerId: {}, name: {}, email: {}",
                providerId, name, email);

        if (providerId == null) {
            throw new IllegalArgumentException("구글 사용자 ID(providerId)가 존재하지 않습니다.");
        }

        // 3. DB에 사용자 등록 또는 기존 사용자 불러오기
        User user = findOrCreateUser(providerId, name, email, defaultProfileUrl);
        log.info("사용자 DB 처리 완료 - userId: {}, provider: {}, createdAt: {}",
                user.getUserId(), user.getProvider(), user.getCreatedAt());

        // 4. JWT 토큰 생성
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwt = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createOrUpdateRefreshToken(user.getUserId());

        log.info("JWT 및 Refresh Token 생성 완료");

        // 5. 신규 사용자 여부 판단
        boolean isNewUser = user.getCreatedAt() != null &&
                (System.currentTimeMillis() - user.getCreatedAt().getTime()) < 5000;

        // 6. 안전한 Map 생성 (null 허용)
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", user.getUserId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        userMap.put("profileImageUrl", user.getProfileImageUrl());

        
        // 주소 복호화
        String decryptedAddress = "";
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            try {
                decryptedAddress = AESUtil.decrypt(user.getAddress());
            } catch (Exception e) {
                log.warn("주소 복호화 실패: {}", e.getMessage());
            }
        }
        userMap.put("address", decryptedAddress);

        // 7. 최종 응답 구성
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", jwt);
        result.put("refreshToken", refreshToken);
        result.put("isNewUser", isNewUser);
        result.put("user", userMap);

        log.info("[GoogleAuthService] 로그인 처리 완료 - isNewUser: {}, user: {}", isNewUser, userMap);
        return result;
    }

    /**
     * 기존 메서드 (하위 호환성을 위해 유지)
     */
    private User findOrCreateUser(String providerId, String name, String email, String defaultProfileUrl) {
        log.info("[findOrCreateUser] providerId: {}, name: {}, email: {}", providerId, name, email);

        Optional<User> existingUser = userRepository.findByProviderIdAndProvider(providerId, "google");

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.info("기존 사용자 발견 - userId: {}", user.getUserId());

            // 사용자 정보 업데이트 (필요 시)
            user.updateSocialUserInfo(name, email, defaultProfileUrl);
            userRepository.save(user);
            log.info("기존 사용자 정보 업데이트 완료");
            return user;
        }

        // 새 사용자 생성
        User newUser = new User(providerId, "google", name, email, defaultProfileUrl);
        userRepository.save(newUser);
        log.info("신규 사용자 생성 완료 - userId: {}", newUser.getUserId());
        return newUser;
    }
}
