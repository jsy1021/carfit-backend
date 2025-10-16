package backend.user.controller;

import backend.user.service.S3Service;
import backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final S3Service s3Service;

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/profile/image/upload")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "업로드할 파일이 없습니다."));
        }

        try {
            // 기존 프로필 이미지 삭제
            String existingUrl = userService.getProfileImageUrl(userDetails.getUsername());
            if (existingUrl != null && !existingUrl.contains("default.png")) {
                int index = existingUrl.lastIndexOf("/");
                if (index != -1) {
                    String key = existingUrl.substring(existingUrl.indexOf("profile/"));
                    s3Service.deleteFile(key);
                    log.info("기존 프로필 이미지 삭제 성공 - key: {}", key);
                }
            }

            // 새 이미지 S3 업로드
            String folder = "profile/" + userDetails.getUsername() + "/";
            String s3Url = s3Service.uploadFile(file, folder);

            // DB에 프로필 이미지 URL 업데이트
            userService.updateProfileImage(userDetails.getUsername(), s3Url);

            log.info("프로필 이미지 업로드 성공: userId={}, s3Url={}", userDetails.getUsername(), s3Url);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로필 이미지가 업로드되었습니다.",
                    "profileImageUrl", s3Url
            ));

        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패: userId={}, error={}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "이미지 업로드 중 오류가 발생했습니다."));
        }
    }
}