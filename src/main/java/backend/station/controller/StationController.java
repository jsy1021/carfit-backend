package backend.station.controller;

import backend.station.dto.StationResponse;
import backend.station.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyStations(
            @RequestParam(name = "x", required = false, defaultValue = "126.9780") double x,
            @RequestParam(name = "y", required = false, defaultValue = "37.5665") double y,
            @RequestParam(name = "sort", defaultValue = "1") String sort,
            @RequestParam(name = "radius", defaultValue = "1000") int radius
    ) {
        try {
            StationResponse response = stationService.getNearbyStations(x, y, sort, radius);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주유소 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "주유소 목록 조회 실패", "message", e.getMessage()));
        }
    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getStationDetails(@RequestParam("id") String stationId) {
        try {
            Map<String, Object> result = stationService.getStationDetails(stationId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("주유소 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "주유소 상세 정보 조회 실패", "message", e.getMessage()));
        }
    }
}
