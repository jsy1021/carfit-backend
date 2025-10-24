package backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor //기본생성자
@AllArgsConstructor //모든 필드에 대한 생성자
@Builder
public class SocialProfileRequestDto {

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자로 입력해주세요. (예: 19900101)")
    private String birthDate;
}
