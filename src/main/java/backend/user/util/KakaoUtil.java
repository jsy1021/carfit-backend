package backend.user.util;

import lombok.RequiredArgsConstructor;
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

import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoUtil {

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;


    // 카카오 로그인 2단계 : 인가코드 -> 액세스토큰 받기
    public String getAccessToken(String code) {

        String accessToken = "";
        String refreshToken = "";
        String reqUrl = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 필수 헤더 세팅
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();

            // 필수 쿼리 파라미터 세팅
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=").append(clientId);
            sb.append("&redirect_uri=").append(redirectUri);
            sb.append("&code=").append(code);
            sb.append("&client_secret=").append(clientSecret);

            bw.write(sb.toString());
            bw.flush();

            int responseCode = conn.getResponseCode();
            log.info("[KakaoApi.getAccessToken] responseCode = {}", responseCode);

            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
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
            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

            br.close();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    // 카카오 로그인 3단계 : 액세스토큰 -> 사용자 정보 받기
    public HashMap<String, Object> getUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String reqUrl = "https://kapi.kakao.com/v2/user/me";
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            int responseCode = conn.getResponseCode();
            log.info("[KakaoApi.getUserInfo] responseCode : {}", responseCode);

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

            // 1. 최상위 id
            String id = element.getAsJsonObject().get("id").getAsString();

            // 2. 닉네임
            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            String nickname = properties.get("nickname").getAsString();

            // 3. 프로필 이미지
            String profileImageUrl = null;
            if (properties.has("profile_image")) {
                profileImageUrl = properties.get("profile_image").getAsString();
            }

            // 4. 이메일 (kakao_account 내부)
            JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();
            String email = null;
            if (kakaoAccount.has("email")) {
                email = kakaoAccount.get("email").getAsString();
            }

            // 결과 map에 담기
            userInfo.put("id", id);
            userInfo.put("nickname", nickname);
            userInfo.put("email", email);
            userInfo.put("profile_image_url", profileImageUrl);

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return userInfo;
    }
}
