package backend.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3Service s3Service;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockFile = new MockMultipartFile(
                "file",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy image content".getBytes()
        );

        // private 필드 bucketName 주입
        ReflectionTestUtils.setField(s3Service, "bucketName", "mock-bucket");
    }

    @Test
    void testUploadFile_Success() throws Exception {
        String folder = "profile/";

        // UUID.randomUUID()를 고정값으로 mocking
        UUID fixedUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        try (var mockedStaticUUID = mockStatic(UUID.class)) {
            mockedStaticUUID.when(UUID::randomUUID).thenReturn(fixedUuid);

            String expectedFileName = folder + fixedUuid + "_" + mockFile.getOriginalFilename();
            String expectedUrl = "https://mock-bucket.s3.amazonaws.com/" + expectedFileName;

            // putObject는 반환값이 있으므로 thenReturn으로 모킹
            when(amazonS3.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(null); // 실제 반환값은 테스트에서 사용 안하므로 null로 처리

            when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL(expectedUrl));

            String result = s3Service.uploadFile(mockFile, folder);

            assertEquals(expectedUrl, result);

            verify(amazonS3, times(1)).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
            verify(amazonS3, times(1)).getUrl(anyString(), anyString());
        }
    }

    @Test
    void testUploadFile_Failure() throws Exception {
        String folder = "profile/";

        doThrow(new RuntimeException("S3 오류")).when(amazonS3)
                .putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            s3Service.uploadFile(mockFile, folder);
        });

        assertTrue(exception.getMessage().contains("S3 업로드 실패"));
    }

    @Test
    void testDeleteFile_Exists() {
        String fileKey = "profile/test.png";

        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        assertDoesNotThrow(() -> s3Service.deleteFile(fileKey));

        verify(amazonS3, times(1)).doesObjectExist(anyString(), anyString());
        verify(amazonS3, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    void testDeleteFile_NotExists() {
        String fileKey = "profile/test.png";

        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(false);

        assertDoesNotThrow(() -> s3Service.deleteFile(fileKey));

        verify(amazonS3, times(1)).doesObjectExist(anyString(), anyString());
        verify(amazonS3, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void testDeleteFile_Failure() {
        String fileKey = "profile/test.png";

        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
        doThrow(new RuntimeException("삭제 실패")).when(amazonS3).deleteObject(anyString(), anyString());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> s3Service.deleteFile(fileKey));
        assertTrue(exception.getMessage().contains("S3 삭제 실패"));
    }
}