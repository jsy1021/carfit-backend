package backend.geolocation.controller;

import backend.geolocation.util.CoordinateConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api")

public class GeoLocationController {

    @Value("${naver.api.client.id}")
    private String clientId;

    @Value("${naver.api.client.secret}")
    private String clientSecret;


    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> getCoordinates(@RequestParam("address") String address) {
        final Logger logger = LoggerFactory.getLogger(GeoLocationController.class);

        try {
            logger.info("Received request for address: {}", address);  // <-- 요청 들어올 때 찍기
            // URL 인코딩 처리
            String apiUrl = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;

            logger.info("Requesting Naver API with URL: {}", apiUrl);

            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-ncp-apigw-api-key-id", clientId);
            headers.set("x-ncp-apigw-api-key", clientSecret);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // API 요청
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

            logger.info("Response from Naver API: {}", response.getBody());

            // 받은 좌표 데이터를 JSON 파싱 후 반환
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseData = objectMapper.readTree(response.getBody());
            JsonNode addresses = responseData.path("addresses");
            logger.info("주소확인{}", addresses);
            if (addresses.isEmpty() || addresses.get(0) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "주소를 찾을 수 없습니다"));
            }
            JsonNode coordinates = addresses.get(0);
            // WGS84 → TM128 변환
            double[] katecCoordinates = CoordinateConverter.wgs84ToTm128(coordinates.path("x").asDouble(), coordinates.path("y").asDouble());

            Double x=katecCoordinates[0];
            Double y=katecCoordinates[1];
            // 결과 반환
            Map<String, Object> result = new HashMap<>();
            result.put("x", x);
            result.put("y", y);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "좌표 변환 실패", "message", e.getMessage()));
        }
    }
}
