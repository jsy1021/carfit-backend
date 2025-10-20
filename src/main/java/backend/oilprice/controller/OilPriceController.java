package backend.oilprice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OilPriceController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${opinet.api.key}")
    private String apiKey;

    @GetMapping("/oil/avg-sigun-price")
    public ResponseEntity<Map<String, Object>> getAvgSigunPrice(
            @RequestParam("sido") String sido,
            @RequestParam(value = "sigun", required = false) String sigun) {
        
        log.info("=== OilPriceController.getAvgSigunPrice 시작 ===");
        log.info("시도 코드: {}, 시군구 코드: {}", sido, sigun);

        try {
            // Opinet API URL 구성
            String apiUrl = "http://www.opinet.co.kr/api/avgSigunPrice.do?out=json&sido=" + sido;
            if (sigun != null && !sigun.isEmpty()) {
                apiUrl += "&sigun=" + sigun;
            }
            apiUrl += "&code=" + apiKey;

            log.info("Opinet API 요청 URL: {}", apiUrl);

            // API 호출
            String response = restTemplate.getForObject(apiUrl, String.class);
            log.info("Opinet API 응답: {}", response);

            if (response == null || response.isEmpty()) {
                log.error("Opinet API 응답이 비어있습니다.");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "API 응답이 비어있습니다."));
            }

            // JSON 파싱
            JsonNode responseData = objectMapper.readTree(response);
            JsonNode result = responseData.path("RESULT");
            JsonNode oil = result.path("OIL");

            if (oil.isEmpty()) {
                log.warn("해당 지역의 유가 정보가 없습니다. sido: {}, sigun: {}", sido, sigun);
                return ResponseEntity.ok(Map.of(
                        "message", "해당 지역의 유가 정보가 없습니다.",
                        "sido", sido,
                        "sigun", sigun != null ? sigun : "전체"
                ));
            }

            // 응답 데이터 구성
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("sido", sido);
            resultMap.put("sigun", sigun != null ? sigun : "전체");
            resultMap.put("oilPrices", oil);
            resultMap.put("totalCount", oil.size());

            log.info("유가 정보 조회 완료. 총 {}개 지역", oil.size());
            return ResponseEntity.ok(resultMap);

        } catch (Exception e) {
            log.error("Opinet API 호출 실패 - sido: {}, sigun: {}, 오류: {}", 
                    sido, sigun, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "유가 정보 조회 실패", "message", e.getMessage()));
        }
    }
}