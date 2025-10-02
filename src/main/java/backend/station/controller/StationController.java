package backend.station.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Value("${opinet.api.key}")
    private String apiKey;

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyStations(
            @RequestParam(name = "x", required = false, defaultValue = "126.9780") double x,
            @RequestParam(name = "y", required = false, defaultValue = "37.5665") double y,
            @RequestParam(name = "sort", defaultValue = "1") String sort,
            @RequestParam(name = "radius", defaultValue = "1000") int radius
    ) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();


        try {
            String url = "http://www.opinet.co.kr/api/aroundAll.do?" +
                    "code="+apiKey+
                    "&x="+x+
                    "&y="+y+
                    "&radius="+radius+
                    "&sort="+sort+
                    "&prodcd=B027&out=json";

            String json = restTemplate.getForObject(url, String.class);
            result.put("stations", mapper.readTree(json).get("RESULT").get("OIL"));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body(Map.of("error", "데이터 파싱 오류"));
        }

        return ResponseEntity.ok(result);
    }
}
