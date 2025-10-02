package backend.auth.service;

import backend.auth.dto.AuthNumberResponse;

import backend.common.util.RandomGenerator;
import backend.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthMailService {

    private static final Long EXPIRATION = 180000L; // 3분
    private final RedisUtil redisUtil;
    private final RandomGenerator randomGenerator;
    private final SendMailService sendMailService;

    /**
     * 이메일로 인증번호 발송
     * @param email 수신자 이메일
     * @param key Redis 저장용 키 (이메일 해시값 등)
     * @return 발송된 인증번호 정보
     */
    @Transactional
    public AuthNumberResponse sendCodeEmail(String email, Long key) {
        int authNumber = randomGenerator.makeRandomNumber();
        String title = "회원 가입 인증 이메일입니다.";
        String content = buildEmailContent(authNumber);

        // 이메일 전송
        sendMailService.sendEmail(email, title, content);

        // 레디스에 인증번호 저장
        redisUtil.saveAuthNumber(key, String.valueOf(authNumber), EXPIRATION);
        log.info("인증번호 발송 완료: email={}, key={}", email, key);
        
        return new AuthNumberResponse(authNumber);
    }

    /**
     * 인증번호 검증
     * @param key Redis 저장용 키
     * @param authNumber 사용자가 입력한 인증번호
     * @return 검증 성공 여부
     */
    public boolean verifyAuthNumber(Long key, Integer authNumber) {
        Object storedAuthNumber = redisUtil.findEmailAuthNumberByKey(key);
        
        if (storedAuthNumber == null) {
            log.warn("인증번호 검증 실패: 저장된 인증번호가 없음, key={}", key);
            return false;
        }

        boolean isValid = storedAuthNumber.toString().equals(authNumber.toString());
        
        if (isValid) {
            log.info("인증번호 검증 성공: key={}", key);
            // 인증 성공 시 Redis에서 삭제 (일회성)
            redisUtil.deleteAuthNumber(key);
        } else {
            log.warn("인증번호 검증 실패: 번호 불일치, key={}", key);
        }
        
        return isValid;
    }

    /**
     * 이메일 본문 생성 (HTML 형식)
     */
    private String buildEmailContent(int authNumber) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #333;">회원가입 인증번호</h2>
                <p>안녕하세요,</p>
                <p>회원가입을 위한 인증번호는 다음과 같습니다:</p>
                <div style="background-color: #f5f5f5; padding: 20px; margin: 20px 0; border-radius: 5px;">
                    <h1 style="color: #007bff; text-align: center; margin: 0;">%d</h1>
                </div>
                <p>이 인증번호는 <strong>3분 동안</strong> 유효합니다.</p>
                <p>본인이 요청하지 않은 경우, 이 이메일을 무시하셔도 됩니다.</p>
                <hr style="margin-top: 30px; border: none; border-top: 1px solid #ddd;">
                <p style="color: #888; font-size: 12px;">이 이메일은 자동으로 발송되었습니다.</p>
            </body>
            </html>
            """, authNumber);
    }
}
