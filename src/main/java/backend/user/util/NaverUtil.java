package backend.user.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

@Slf4j
@Component
public class NaverUtil {

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${NAVER_REDIRECT_URI}")
    private String redirectUri;

    /**
     * 네이버 인증 코드로 액세스 토큰 발급
     */
    public String getAccessToken(String code) {
        String accessToken = "";
        String reqURL = "https://nid.naver.com/oauth2.0/token";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // POST 요청을 위해 기본값이 false인 setDoOutput을 true로 설정
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=" + clientId);
            sb.append("&client_secret=" + clientSecret);
            sb.append("&code=" + code);
            sb.append("&state=state");

            bw.write(sb.toString());
            bw.flush();

            // 결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            log.info("[NaverApi.getAccessToken] responseCode : {}", responseCode);

            // 요청을 통해 얻은 JSON타입의 Response 메시지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            log.info("responseBody={}", result);

            // JSON 파싱
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            accessToken = element.getAsJsonObject().get("access_token").getAsString();

            br.close();
            bw.close();
        } catch (Exception e) {
            log.error("네이버 액세스 토큰 획득 실패", e);
        }
        return accessToken;
    }

    /**
     * 네이버 액세스 토큰으로 사용자 정보 조회
     */
    public HashMap<String, Object> getUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String reqUrl = "https://openapi.naver.com/v1/nid/me";
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            log.info("[NaverApi.getUserInfo] responseCode : {}", responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String line = "";
            StringBuilder responseSb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseSb.append(line);
            }
            String result = responseSb.toString();
            log.info("responseBody = {}", result);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            JsonElement responseElement = element.getAsJsonObject().get("response");
            
            // 네이버 API 응답 구조: { "response": { "id": "...", "email": "...", "name": "..." } }
            String id = responseElement.getAsJsonObject().get("id").getAsString();
            String name = responseElement.getAsJsonObject().get("name") != null 
                    ? responseElement.getAsJsonObject().get("name").getAsString() 
                    : "";
            String email = responseElement.getAsJsonObject().get("email") != null
                    ? responseElement.getAsJsonObject().get("email").getAsString()
                    : "";
            String profileImageUrl = responseElement.getAsJsonObject().get("profile_image") != null
                    ? responseElement.getAsJsonObject().get("profile_image").getAsString()
                    : "";

            userInfo.put("id", id);
            userInfo.put("name", name);
            userInfo.put("email", email);
            userInfo.put("profile_image_url", profileImageUrl);

            br.close();
        } catch (Exception e) {
            log.error("네이버 사용자 정보 조회 실패", e);
        }
        return userInfo;
    }
}



