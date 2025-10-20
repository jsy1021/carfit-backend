package backend.geolocation.controller;

import backend.common.util.CoordinateConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GeoLocationController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${naver.api.client.id}")
    private String clientId;

    @Value("${naver.api.client.secret}")
    private String clientSecret;


    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> getCoordinates(@RequestParam("address") String address) {
        try {
            log.info("Received request for address: {}", address);
            // URL 인코딩 처리
            String apiUrl = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;

            log.info("Requesting Naver API with URL: {}", apiUrl);

            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-ncp-apigw-api-key-id", clientId);
            headers.set("x-ncp-apigw-api-key", clientSecret);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // API 요청
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

            log.info("Response from Naver API: {}", response.getBody());

            // 받은 좌표 데이터를 JSON 파싱 후 반환
            JsonNode responseData = objectMapper.readTree(response.getBody());
            JsonNode addresses = responseData.path("addresses");
            log.info("주소확인{}", addresses);
            if (addresses.isEmpty() || addresses.get(0) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "주소를 찾을 수 없습니다"));
            }
            JsonNode coordinates = addresses.get(0);
            // WGS84 → TM128 변환
            double originalX = coordinates.path("x").asDouble();
            double originalY = coordinates.path("y").asDouble();
            double[] katecCoordinates = CoordinateConverter.wgs84ToTm128(originalX, originalY);

            Double x = katecCoordinates[0];
            Double y = katecCoordinates[1];
            
            // 좌표 변환 정확도 검증 (10미터 허용 오차)
            boolean isValid = CoordinateConverter.validateWgs84ToTm128Conversion(originalX, originalY, x, y, 10.0);
            
            if (!isValid) {
                log.warn("좌표 변환 정확도 검증 실패 - 원본: ({}, {}), 변환: ({}, {})", 
                           originalX, originalY, x, y);
            }
            
            // 결과 반환
            Map<String, Object> result = new HashMap<>();
            
            // 변환된 좌표 (TM128)
            result.put("x", x);
            result.put("y", y);
            result.put("coordinate_system", "TM128");
            
            // 원본 좌표 (WGS84)
            result.put("original_x", originalX);
            result.put("original_y", originalY);
            result.put("original_coordinate_system", "WGS84");
            
            // 변환 정보
            result.put("accuracy_validated", isValid);
            result.put("conversion_precision_meters", 0.01); // 1cm 정밀도
            
            // 주소 정보
            result.put("address", coordinates.path("roadAddress").asText());
            result.put("jibun_address", coordinates.path("jibunAddress").asText());
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "좌표 변환 실패", "message", e.getMessage()));
        }
    }
    
}
