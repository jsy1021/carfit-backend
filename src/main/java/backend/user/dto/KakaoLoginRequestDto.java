package backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginRequestDto {
    
    @NotBlank(message = "인증 코드는 필수입니다.")
    private String code;
    
    @NotBlank(message = "클라이언트 ID는 필수입니다.")
    private String clientId;
    
    @NotBlank(message = "리다이렉트 URI는 필수입니다.")
    private String redirectUri;
    
    @NotBlank(message = "클라이언트 시크릿은 필수입니다.")
    private String clientSecret;
}
