package backend.common.util;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * JWT 시크릿 키 생성 유틸리티
 * 실제 운영환경에서는 이 클래스를 사용해서 안전한 시크릿 키를 생성하고
 * 환경변수나 외부 설정으로 관리해야 합니다.
 */
public class JwtSecretGenerator {
    
    public static void main(String[] args) {
        // 256비트 (32바이트) 시크릿 키 생성
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        
        System.out.println("생성된 JWT 시크릿 키:");
        System.out.println(secretKey);
        System.out.println("\n환경변수로 설정:");
        System.out.println("export JWT_SECRET=" + secretKey);
    }
}

