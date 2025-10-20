package backend.user.controller;

import backend.auth.security.JwtAuthenticationFilter;
import backend.auth.security.JwtUtil;
import backend.user.service.S3Service;
import backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file", // 반드시 Controller의 @RequestParam 이름과 동일해야 함
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy image content".getBytes()
        );
    }

    //  인증 안 된 경우 401
    @Test
    void testUploadProfileImage_Unauthenticated() throws Exception {
        mockMvc.perform(multipart("/api/profile/image/upload")
                        .file(mockFile)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


    //  성공 케이스
    @Test
    @WithMockUser(username = "testuser")
    void testUploadProfileImage_Success() throws Exception {
        // S3 업로드 반환값 모킹
        Mockito.when(s3Service.uploadFile(any(), anyString()))
                .thenReturn("https://s3.mock/testuser/profile.png");

        // DB 업데이트 모킹
        Mockito.doNothing().when(userService).updateProfileImage(anyString(), anyString());

        mockMvc.perform(multipart("/api/profile/image/upload")
                        .file(mockFile)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}