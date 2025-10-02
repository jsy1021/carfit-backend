package backend.user.dto;

import backend.user.domain.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor //기본생성자
@AllArgsConstructor //모든 필드에 대한 생성자
@Builder
public class SignupRequestDto {
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4-20자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용 가능합니다.")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 20, message = "비밀번호는 6-20자 사이여야 합니다.")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 10, message = "이름은 2-10자 사이여야 합니다.")
    private String name;
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "주소는 필수입니다.")
    private String address;
    
    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자로 입력해주세요. (예: 19900101)")
    private String birthDate;
    
    @AssertTrue(message = "이용약관에 동의해야 합니다.")
    private boolean termsAgreed;
    
    @AssertTrue(message = "개인정보처리방침에 동의해야 합니다.")
    private boolean privacyAgreed;
    
    private boolean marketingAgreed;



    public User toEntity(String encodedPassword,String encodedAddress,String encodedEmail) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsedBirthDate = LocalDate.parse(this.birthDate, formatter);

        return new User(
                userId,
                encodedPassword,
                name,
                encodedEmail,
                encodedAddress,
                parsedBirthDate,
                termsAgreed,
                privacyAgreed,
                marketingAgreed
        );
    }
}

