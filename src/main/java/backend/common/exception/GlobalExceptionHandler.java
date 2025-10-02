package backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 인증 실패 예외
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleAuthenticationException(Exception e) {
        log.warn("인증 실패: {}", e.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "인증 실패");
        error.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 잘못된 인수 예외 (중복 아이디 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "잘못된 요청");
        error.put("message", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 유효성 검증 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("유효성 검증 실패: {}", e.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "유효성 검증 실패");
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        error.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // JWT 관련 예외
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleJwtExpiredException(io.jsonwebtoken.ExpiredJwtException e) {
        log.warn("JWT 토큰 만료: {}", e.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "토큰 만료");
        error.put("message", "토큰이 만료되었습니다. 다시 로그인해주세요.");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<Map<String, String>> handleJwtException(io.jsonwebtoken.JwtException e) {
        log.warn("JWT 토큰 오류: {}", e.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "토큰 오류");
        error.put("message", "유효하지 않은 토큰입니다.");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 기타 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "서버 오류");
        error.put("message", "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

