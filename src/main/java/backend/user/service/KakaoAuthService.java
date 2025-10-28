package backend.user.service;

import backend.auth.security.CustomUserDetails;
import backend.auth.security.JwtUtil;
import backend.auth.service.RefreshTokenService;
import backend.common.util.AESUtil;
import backend.user.domain.User;
import backend.user.repository.UserRepository;
import backend.user.util.KakaoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${aws.s3.default-profile-url}")
    private String defaultProfileUrl;

    /**
     * 카카오 인증 코드로 로그인 처리
     * @param code 카카오 인증 코드
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
            String providerId = (String) kakaoUserInfo.get("id");
            String nickname = (String) kakaoUserInfo.get("nickname");
            String email = kakaoUserInfo.get("email") != null ? (String) kakaoUserInfo.get("email") : "";


            log.info("카카오 사용자 정보 - ID: {}, 닉네임: {}, 이메일: {}", providerId, nickname, email);

            // 3. DB에서 사용자 조회 또는 생성 (최적화된 버전)
            User user = findOrCreateUser(providerId, nickname, email, defaultProfileUrl);

            // 4. 백엔드 JWT 토큰 생성
            CustomUserDetails userDetails = new CustomUserDetails(user);
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = refreshTokenService.createOrUpdateRefreshToken(user.getUserId());

            // 5. 응답 데이터 구성
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("name", user.getName() != null ? user.getName() : "");
            userMap.put("email", user.getEmail() != null ? user.getEmail() : "");

            String decryptedAddress = "";
            if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                try {
                    decryptedAddress = AESUtil.decrypt(user.getAddress());
                } catch (Exception e) {
                    log.warn("주소 복호화 실패: {}", e.getMessage());
                }
            }
            userMap.put("address", decryptedAddress);
            userMap.put("profileImageUrl", user.getProfileImageUrl());
            userMap.put("role", user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("user", userMap);
            response.put("isNewUser", user.getCreatedAt().getTime() > System.currentTimeMillis() - 5000); // 5초 이내 생성된 사용자

            log.info("카카오 로그인 성공 - 사용자: {}", user.getUserId());
            return response;

        } catch (Exception e) {
            log.error("카카오 로그인 처리 실패", e);
            throw new RuntimeException("카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 기존 메서드 (하위 호환성을 위해 유지)
     */
    private User findOrCreateUser(String providerId, String nickname, String email, String profileImageUrl) {
        // 1. 소셜 ID로 기존 사용자 조회
        Optional<User> existingUser = userRepository.findByProviderIdAndProvider(providerId, "kakao");
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // 기존 사용자 정보 업데이트 (닉네임, 이메일, 프로필 이미지가 변경될 수 있음)
            log.info("기존 카카오 사용자 정보 업데이트 - 이름: {}, 이메일: {}, 프로필: {}", 
                     nickname, email, profileImageUrl);
            user.updateSocialUserInfo(nickname, email, profileImageUrl);
            userRepository.save(user);
            log.info("기존 카카오 사용자 로그인 완료: {}", user.getUserId());
            return user;
        }

        // 2. 새 사용자 생성 (동시성 문제 처리)
        try {
            User newUser = new User(providerId, "kakao", nickname, email, profileImageUrl);
            userRepository.save(newUser);
            log.info("새 카카오 사용자 생성 완료: {}", newUser.getUserId());
            return newUser;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 동시성 문제로 인해 이미 생성된 경우 기존 사용자 조회
            log.warn("사용자 생성 중 중복 에러 발생, 기존 사용자 조회: {}", e.getMessage());
            Optional<User> duplicateUser = userRepository.findByUserId("kakao_" + providerId);
            
            if (duplicateUser.isPresent()) {
                User user = duplicateUser.get();
                log.info("중복 방지 - 기존 카카오 사용자로 반환: {}", user.getUserId());
                // 최신 카카오 정보로 업데이트
                user.updateSocialUserInfo(nickname, email, profileImageUrl);
                userRepository.save(user);
                return user;
            }
            
            throw new RuntimeException("사용자 생성에 실패했습니다.", e);
        }
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
