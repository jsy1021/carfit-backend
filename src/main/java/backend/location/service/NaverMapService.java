package backend.location.service;

import backend.location.dto.GeocodingResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverMapService {

    @Value("${naver.api.client.id}")
    private String clientId;

    @Value("${naver.api.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeocodingResult getCoordinates(String address) {
        log.info("=== NaverMapService.getCoordinates 시작 ===");
        log.info("입력받은 주소: {}", address);
        
        try {
            // GeoLocationController와 동일한 URL 사용
            String url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;
            

            // GeoLocationController와 동일한 헤더 사용 (소문자)
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-ncp-apigw-api-key-id", clientId);
            headers.set("x-ncp-apigw-api-key", clientSecret);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            log.info("요청 헤더: {}", headers);

            // GeoLocationController와 동일한 방식으로 요청
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            log.info("API 응답 상태: {}", response.getStatusCode());
            log.info("API 응답 헤더: {}", response.getHeaders());
            log.info("API 응답 본문: {}", response.getBody());

            if (response.getBody() == null) {
                log.error("API 응답 본문이 null입니다.");
                throw new IllegalArgumentException("API 응답이 비어있습니다.");
            }

            // GeoLocationController와 동일한 방식으로 JSON 파싱
            JsonNode responseData = objectMapper.readTree(response.getBody());
            JsonNode addresses = responseData.path("addresses");
            log.info("주소 확인: {}", addresses);
            
            if (addresses.isEmpty() || addresses.get(0) == null) {
                log.warn("주소 검색 결과가 없습니다.");
                throw new IllegalArgumentException("주소를 찾을 수 없습니다.");
            }

            JsonNode coordinates = addresses.get(0);
            log.info("첫 번째 검색 결과: {}", coordinates);
            
            double lat = coordinates.path("y").asDouble();
            double lng = coordinates.path("x").asDouble();
            
            // 주소 정보 추출
            String roadAddress = coordinates.path("roadAddress").asText();
            String jibunAddress = coordinates.path("jibunAddress").asText();
            
            log.info("변환된 좌표 - 위도: {}, 경도: {}", lat, lng);
            log.info("도로명주소: {}", roadAddress);
            log.info("지번주소: {}", jibunAddress);

            return GeocodingResult.builder()
                    .latitude(lat)
                    .longitude(lng)
                    .roadAddress(roadAddress)
                    .jibunAddress(jibunAddress)
                    .success(true)
                    .build();

        } catch (NumberFormatException e) {
            log.error("좌표 변환 실패 - 숫자 형식 오류: {}", e.getMessage());
            return GeocodingResult.builder()
                    .success(false)
                    .build();
        } catch (Exception e) {
            log.error("네이버 지도 API 호출 실패 - 주소: {}, 오류: {}", address, e.getMessage(), e);
            return GeocodingResult.builder()
                    .success(false)
                    .build();
        }
    }
}
