package backend.auth.security;


import backend.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }
    // 1. 권한(역할) 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    // 2. 비밀번호 반환
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 3. 사용자명 반환 (로그인 ID 기준)
    @Override
    public String getUsername() {
        return user.getUserId(); // 또는 email
    }

    // 4~7. 계정 상태 관련 (기본 true로 두어도 무방)
    @Override
    public boolean isAccountNonExpired() {
        return true; // 또는 user.isExpired() == false
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 또는 user.isLocked() == false
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 처리 필요시 수정
    }

    @Override
    public boolean isEnabled() {
        return true; // 또는 user.isActivated() == true
    }

    // User 객체에 직접 접근하고 싶을 경우 getter 추가
    public User getUser() {
        return user;
    }
}
