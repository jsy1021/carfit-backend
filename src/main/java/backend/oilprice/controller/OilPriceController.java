package backend.oilprice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OilPriceController {

    @Value("${opinet.api.key}")
    private String apiKey;

    @GetMapping("/oil")
    public ResponseEntity<Map<String, Object>> getOilPrices() {
        String baseUrl = "http://www.opinet.co.kr/api/avgSidoPrice.do?out=json&code=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        String gasolineUrl = baseUrl + "&prodcd=B027";
        String dieselUrl = baseUrl + "&prodcd=D047";
        String lpgUrl= baseUrl + "&prodcd=K015";

        String gasolineJson = restTemplate.getForObject(gasolineUrl, String.class);
        String dieselJson = restTemplate.getForObject(dieselUrl, String.class);
        String lpgJson =restTemplate.getForObject(lpgUrl, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("gasoline", mapper.readTree(gasolineJson).get("RESULT").get("OIL"));
            result.put("diesel", mapper.readTree(dieselJson).get("RESULT").get("OIL"));
            result.put("lpg",mapper.readTree(lpgJson).get("RESULT").get("OIL"));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body(Map.of("error", "데이터 파싱 오류"));
        }

        return ResponseEntity.ok(result);
    }
}