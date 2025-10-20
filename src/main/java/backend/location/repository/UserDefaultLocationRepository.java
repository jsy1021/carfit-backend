package backend.location.repository;

import backend.location.domain.UserDefaultLocation;
import backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserDefaultLocationRepository extends JpaRepository<UserDefaultLocation, Long> {
    Optional<UserDefaultLocation> findByUser(User user);
}
