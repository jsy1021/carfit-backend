package backend.profile.controller;

import backend.profile.dto.UpdateProfileRequest;
import backend.profile.dto.UserProfileResponse;
import backend.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 내 프로필 조회
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserProfileResponse profile = profileService.getMyProfile(userDetails.getUsername());
            log.info("프로필 조회 성공: userId={}", userDetails.getUsername());
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            log.warn("프로필 조회 실패: userId={}, error={}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("프로필 조회 실패: userId={}, error={}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 내 프로필 수정
     */
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            UserProfileResponse updated = profileService.updateMyProfile(userDetails.getUsername(), request);
            log.info("프로필 수정 성공: userId={}, avatarUrl={}", userDetails.getUsername(), request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("프로필 수정 실패: userId={}, error={}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("프로필 수정 중 오류: userId={}, error={}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


