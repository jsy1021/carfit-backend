package backend.profile.service;

import backend.profile.dto.UpdateProfileRequest;
import backend.profile.dto.UserProfileResponse;
import backend.user.domain.User;
import backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;

    /**
     * 프로필 조회
     */
    public UserProfileResponse getMyProfile(String userId) throws Exception {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    /**
     * 프로필 수정 (닉네임/소개)
     */
    @Transactional
    public UserProfileResponse updateMyProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        user.updateAvatar(request.getAvatarUrl());
        userRepository.save(user);
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}

