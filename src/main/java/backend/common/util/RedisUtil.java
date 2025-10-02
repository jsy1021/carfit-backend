package backend.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 이메일 인증번호 저장
     * @param key 이메일 해시값 또는 고유 키
     * @param emailAuthNumber 인증번호
     * @param expiration 만료 시간(밀리초)
     */
    public void saveAuthNumber(Long key, String emailAuthNumber, Long expiration) {
        String redisKey = "AuthNumber:" + key;
        redisTemplate.opsForValue().set(redisKey, emailAuthNumber, expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * 이메일 인증번호 조회
     * @param key 이메일 해시값 또는 고유 키
     * @return 저장된 인증번호
     */
    public Object findEmailAuthNumberByKey(Long key) {
        String redisKey = "AuthNumber:" + key;
        return redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 이메일 인증번호 삭제
     * @param key 이메일 해시값 또는 고유 키
     */
    public void deleteAuthNumber(Long key) {
        String redisKey = "AuthNumber:" + key;
        redisTemplate.delete(redisKey);
    }
}

