package backend.user.service;

import backend.user.domain.User;
import backend.user.dto.LoginRequestDto;
import backend.user.dto.SignupRequestDto;
import backend.user.dto.SocialProfileRequestDto;
import backend.user.repository.UserRepository;
import backend.common.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByUserId(String userId){
        return userRepository.findByUserId(userId).isPresent();
    }

    public void registerUser(SignupRequestDto signupRequestDto) throws Exception {
        //중복 아이디 검사
        String userId=signupRequestDto.getUserId();
        if(userRepository.findByUserId(userId).isPresent()){

            throw new IllegalArgumentException("동일한 아이디가 존재합니다.");
        }
        String encodedPassword=passwordEncoder.encode(signupRequestDto.getPassword());
        String encodedAddress= AESUtil.encrypt(signupRequestDto.getAddress());
        String encodedEmail= AESUtil.encrypt(signupRequestDto.getEmail());
        User user =signupRequestDto.toEntity(encodedPassword,encodedAddress, encodedEmail);

        userRepository.save(user);
    }

    public boolean login(LoginRequestDto loginRequestDto) {
        User user=userRepository.findByUserId(loginRequestDto.getUserId())
                .orElseThrow(()->new IllegalArgumentException("해당 Id가 존재하지 않습니다."));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return true;
    }

    /**
     * 기존 프로필 이미지 URL 조회
     */
    public String getProfileImageUrl(String userId) {
        return userRepository.findByUserId(userId)
                .map(User::getProfileImageUrl)
                .orElse(null);
    }

    /**
     * 프로필 이미지 업데이트
     */
    public void updateProfileImage(String userId, String profileImageUrl) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        user.updateProfileImage(profileImageUrl);
        userRepository.save(user);
    }

    /**
     * 소셜 로그인 사용자 추가 정보 업데이트 (주소, 생년월일)
     */
    public void updateSocialUserProfile(String userId, SocialProfileRequestDto requestDto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 소셜 로그인 사용자인지 확인
        if (!user.isSocialUser()) {
            throw new IllegalArgumentException("소셜 로그인 사용자가 아닙니다");
        }

        try {
            // 생년월일 파싱 (YYYYMMDD -> LocalDate)
            LocalDate birthDate = LocalDate.parse(requestDto.getBirthDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 주소 암호화
            String encodedAddress = AESUtil.encrypt(requestDto.getAddress());
            
            // 사용자 정보 업데이트
            user.updateSocialUserProfile(requestDto.getAddress(), birthDate, encodedAddress);
            userRepository.save(user);
            
        } catch (Exception e) {
            throw new IllegalArgumentException("사용자 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 소셜 로그인 사용자 프로필 정보 조회
     */
    public SocialProfileRequestDto getSocialUserProfile(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (!user.isSocialUser()) {
            throw new IllegalArgumentException("소셜 로그인 사용자가 아닙니다");
        }

        try {
            // 암호화된 주소 복호화
            String decryptedAddress = AESUtil.decrypt(user.getAddress());
            
            // 생년월일을 YYYYMMDD 형식으로 변환
            String birthDateStr = user.getBirthDate() != null 
                ? user.getBirthDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                : null;

            return SocialProfileRequestDto.builder()
                    .address(decryptedAddress)
                    .birthDate(birthDateStr)
                    .build();
                    
        } catch (Exception e) {
            throw new IllegalArgumentException("사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}

