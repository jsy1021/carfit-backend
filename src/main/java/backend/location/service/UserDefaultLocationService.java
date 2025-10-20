package backend.location.service;

import backend.location.domain.UserDefaultLocation;
import backend.location.dto.GeocodingResult;
import backend.location.repository.UserDefaultLocationRepository;
import backend.user.domain.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;




@Service
@RequiredArgsConstructor
@Slf4j
public class UserDefaultLocationService {

    private final UserDefaultLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final NaverMapService naverMapService;

    public UserDefaultLocation saveOrUpdateAddress(Long userId, String address) {
        log.info("=== UserDefaultLocationService.saveOrUpdateAddress 시작 ===");
        log.info("사용자 ID: {}, 주소: {}", userId, address);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userId: {}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });
        
        log.info("사용자 정보 조회 성공: {}", user.getUserId());

        // 네이버 지도 API 호출
        GeocodingResult geocodingResult = naverMapService.getCoordinates(address);
        
        if (!geocodingResult.isSuccess()) {
            log.error("좌표 변환 실패: {}", address);
            throw new RuntimeException("주소를 좌표로 변환할 수 없습니다: " + address);
        }
        
        Double latitude = geocodingResult.getLatitude();
        Double longitude = geocodingResult.getLongitude();
        String roadAddress = geocodingResult.getRoadAddress();
        
        log.info("좌표 변환 완료 - 위도: {}, 경도: {}", latitude, longitude);
        log.info("도로명주소: {}", roadAddress);

        return locationRepository.findByUser(user)
                .map(existing -> {   // 기존 위치가 있으면 갱신

                    existing.updateAddress(roadAddress, latitude, longitude);
                    UserDefaultLocation saved = locationRepository.save(existing);
                    log.info("기존 위치 업데이트 완료: {}", saved.getId());
                    return saved;
                })
                .orElseGet(() -> {   // 없으면 새로 생성

                    UserDefaultLocation newLocation = new UserDefaultLocation(user, roadAddress, latitude, longitude);
                    UserDefaultLocation saved = locationRepository.save(newLocation);
                    log.info("새로운 위치 생성 완료: {}", saved.getId());
                    return saved;
                });
    }

    public UserDefaultLocation getUserDefaultLocation(Long userId) {
        log.info("사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userId: {}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        log.info("사용자 정보 조회 성공: {}", user.getUserId());

        return locationRepository.findByUser(user)
                .orElse(null); // 기본 위치가 없으면 null 반환
    }
}