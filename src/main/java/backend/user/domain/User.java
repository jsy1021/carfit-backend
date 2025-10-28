package backend.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@Table(name="users")
@NoArgsConstructor
@Getter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",updatable = false)
    private Long id;

    @Column(name="user_id", updatable = false,unique = true)
    private String userId; //아이디

    @Column(name="password")
    private String password; //비밀번호

    @Column(name="name")
    private String name; //이름

    @Column(name="email")
    private String email; //이메일

    @Column(name="address")
    private String address; //주소

    @Column(name="profile_image_url", length = 500)
    private String profileImageUrl; // 프로필 이미지 URL

    @Column(name="birth_date")
    private LocalDate birthDate; //생년월일

    @Column(name="agreed_terms")
    private boolean termsAgreed;

    @Column(name="agreed_privacy")
    private boolean privacyAgreed;

    @Column(name="agreed_marketing")
    private boolean marketingAgreed;

    @Column(name="created_at")
    private Date createdAt; //생성시점

    @Column(name="role")
    private String role; //user로 저장

    @Column(name="provider_id")
    private String providerId; // 소셜 로그인 ID

    @Column(name="provider")
    private String provider; // 소셜 로그인 제공자 (kakao, google 등)

    public User(String userId, String password, String name, String email, String address,
                LocalDate birthDate, boolean agreedTerms, boolean privacyAgreed, boolean marketingAgreed) {
        this.provider="local";
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.address = address;
        this.profileImageUrl = null; // 기본값은 null (기본 프로필 이미지 사용)
        this.birthDate = birthDate;
        this.termsAgreed = agreedTerms;
        this.privacyAgreed = privacyAgreed;
        this.marketingAgreed = marketingAgreed;
        this.createdAt = new Date();
        this.role = "USER";
    }

    // 프로필 이미지만 업데이트
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    // 실제 사용할 프로필 이미지 URL 반환 (null이면 기본 이미지)
    public String getProfileImageUrl() {
        return this.profileImageUrl == null 
            ? "https://carfit-aws-bucket.s3.ap-northeast-2.amazonaws.com/assets/profile/default.png"  // 기본 이미지 (완전한 URL)
            : this.profileImageUrl;  // 사용자가 설정한 이미지 (S3 URL)
    }

    // 소셜 로그인용 생성자
    public User(String providerId, String provider, String name, String email,String profileImageUrl) {
        this.providerId = providerId;
        this.provider = provider;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.userId = provider + "_" + providerId; // 소셜 로그인용 userId 생성
        this.password = null; // 소셜 로그인은 비밀번호 없음
        this.address = null;
        this.birthDate = null;
        this.termsAgreed = true; // 소셜 로그인 시 기본 동의
        this.privacyAgreed = true;
        this.marketingAgreed = false;
        this.createdAt = new Date();
        this.role = "USER";
    }

    // 소셜 로그인 사용자 정보 업데이트
    public void updateSocialUserInfo(String name, String email, String profileImageUrl) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
        if (email != null && !email.isEmpty()) {
            this.email = email;
        }
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            this.profileImageUrl = profileImageUrl;
        }
        // updatedAt 필드가 있다면 여기서도 업데이트
    }

    // 소셜 로그인 사용자 여부 확인
    public boolean isSocialUser() {
        return this.providerId != null && this.provider != null;
    }

    // 소셜 로그인 사용자 프로필 정보 업데이트 (주소, 생년월일)
    public void updateSocialUserProfile(String address, LocalDate birthDate, String encodedAddress) {
        this.address = encodedAddress; // 암호화된 주소 저장
        this.birthDate = birthDate;
    }
}

