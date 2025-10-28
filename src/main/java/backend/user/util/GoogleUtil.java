package backend.user.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GoogleUtil {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GOOGLE_REDIRECT_URI}")
    private String redirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 구글 인증 코드로 Access Token 발급 요청
     */
    public String getAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 파라미터
        String body = "code=" + code +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            log.info("구글 액세스 토큰 발급 성공: {}", accessToken);
            return accessToken;

        } catch (Exception e) {
            log.error("구글 액세스 토큰 발급 실패", e);
            throw new RuntimeException("구글 액세스 토큰 요청 실패: " + e.getMessage());
        }
    }

    /**
     * 액세스 토큰으로 구글 사용자 정보 가져오기
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", jsonNode.get("id").asText());
            userInfo.put("email", jsonNode.get("email").asText());
            userInfo.put("name", jsonNode.get("name").asText());

            log.info("구글 사용자 정보: {}", userInfo);
            return userInfo;

        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 실패", e);
            throw new RuntimeException("구글 사용자 정보 요청 실패: " + e.getMessage());
        }
    }
}
