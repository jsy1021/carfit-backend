package backend.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    public String uploadFile(MultipartFile file, String folder) {
        log.info("S3Service.uploadFile 호출됨 - folder: {}, originalFileName: {}", folder, file.getOriginalFilename());

        String fileName = folder + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            log.info("S3 업로드 시작 - bucket: {}, fileName: {}, fileSize: {}", bucketName, fileName, file.getSize());
            log.info("파일 정보 - originalName: {}, contentType: {}", file.getOriginalFilename(), file.getContentType());

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);

            String s3Url = amazonS3.getUrl(bucketName, fileName).toString();
            log.info("S3 업로드 성공 - s3Url: {}", s3Url);

            return s3Url;
        } catch (Exception e) {
            log.error("S3 업로드 실패 - bucket: {}, fileName: {}, error: {}", bucketName, fileName, e.getMessage(), e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }
}