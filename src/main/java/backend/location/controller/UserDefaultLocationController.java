package backend.location.controller;

import backend.auth.security.CustomUserDetails;
import backend.location.domain.UserDefaultLocation;
import backend.location.dto.UserDefaultLocationRequest;
import backend.location.dto.UserDefaultLocationResponse;
import backend.location.service.UserDefaultLocationService;
import backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class UserDefaultLocationController {

    private final UserDefaultLocationService locationService;

    @PostMapping("/address")
    public ResponseEntity<UserDefaultLocationResponse> saveAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserDefaultLocationRequest request) {

        log.info("=== UserDefaultLocationController.saveAddress 시작 ===");
        log.info("입력 주소: {}", request.getAddress());

        if (userDetails == null) {
            log.error("인증 정보가 없습니다. JWT 토큰을 확인해주세요.");
            return ResponseEntity.status(401).build();
        }

        User user = userDetails.getUser();
        log.info("사용자: {}", user.getUserId());

        UserDefaultLocation saved = locationService.saveOrUpdateAddress(user.getId(), request.getAddress());

        // DTO로 변환
        UserDefaultLocationResponse response = UserDefaultLocationResponse.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .build();

        log.info("저장 완료: id={}, address={}, lat={}, lng={}", 
                response.getId(), response.getAddress(), response.getLatitude(), response.getLongitude());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/address")
    public ResponseEntity<UserDefaultLocationResponse> getAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("=== UserDefaultLocationController.getAddress 시작 ===");

        if (userDetails == null) {
            log.error("인증 정보가 없습니다. JWT 토큰을 확인해주세요.");
            return ResponseEntity.status(401).build();
        }

        User user = userDetails.getUser();
        log.info("사용자: {}", user.getUserId());

        UserDefaultLocation location = locationService.getUserDefaultLocation(user.getId());
        
        if (location == null) {
            log.info("기본 위치가 설정되지 않음");
            return ResponseEntity.notFound().build();
        }

        // DTO로 변환
        UserDefaultLocationResponse response = UserDefaultLocationResponse.builder()
                .id(location.getId())
                .userId(location.getUser().getId())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();

        log.info("조회 완료: id={}, address={}, lat={}, lng={}", 
                response.getId(), response.getAddress(), response.getLatitude(), response.getLongitude());

        return ResponseEntity.ok(response);
    }
}