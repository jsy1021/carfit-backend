package backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthNumberResponse {
    private int authNumber;
    private String message;

    public AuthNumberResponse(int authNumber) {
        this.authNumber = authNumber;
        this.message = "인증번호가 발송되었습니다.";
    }
}


