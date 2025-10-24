package backend.user.repository;

import backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);//userid로 사용자 정보를 가져옴
    Optional<User> findByEmail(String email);
    
    // 소셜 로그인 관련 메서드
    Optional<User> findBySocialIdAndProvider(String socialId, String provider);
    Optional<User> findBySocialId(String socialId);
    boolean existsBySocialIdAndProvider(String socialId, String provider);
}

