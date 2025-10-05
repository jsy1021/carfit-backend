package backend.station.service;

import backend.common.util.CoordinateConverter;
import backend.station.dto.StationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${opinet.api.key}")
    private String apiKey;

    /**
     * 주변 주유소 목록 조회
     */
    public StationResponse getNearbyStations(double x, double y, String sort, int radius) {
        try {
            // Opinet API 호출
            String url = buildNearbyStationsUrl(x, y, sort, radius);
            String json = restTemplate.getForObject(url, String.class);
            JsonNode oilData = objectMapper.readTree(json).get("RESULT").get("OIL");

            // 응답 데이터 변환
            List<StationResponse.StationInfo> stations = new ArrayList<>();
            int conversionErrors = 0;

            for (JsonNode station : oilData) {
                // TM128 → WGS84 변환
                double tm128X = station.get("GIS_X_COOR").asDouble();
                double tm128Y = station.get("GIS_Y_COOR").asDouble();
                double[] wgs84 = CoordinateConverter.tm128ToWgs84(tm128X, tm128Y);

                // 좌표 변환 정확도 검증 (10미터 허용 오차)
                boolean isValid = CoordinateConverter.validateTm128ToWgs84Conversion(
                        tm128X, tm128Y, wgs84[0], wgs84[1], 10.0);

                if (!isValid) {
                    conversionErrors++;
                    log.warn("주유소 좌표 변환 정확도 검증 실패 - TM128: ({}, {}), WGS84: ({}, {})",
                            tm128X, tm128Y, wgs84[0], wgs84[1]);
                }

                // DTO 생성
                StationResponse.StationInfo stationInfo = StationResponse.StationInfo.builder()
                        .id(station.has("UNI_ID") ? station.get("UNI_ID").asText() : null)
                        .name(station.has("OS_NM") ? station.get("OS_NM").asText() : null)
                        .brand(station.has("POLL_DIV_CD") ? station.get("POLL_DIV_CD").asText() : null)
                        .address(station.has("NEW_ADR") ? station.get("NEW_ADR").asText() : null)
                        .tel(station.has("TEL") ? station.get("TEL").asText() : null)
                        .price(station.has("PRICE") ? station.get("PRICE").asText() : null)
                        .latitude(wgs84[1])
                        .longitude(wgs84[0])
                        .distance(station.has("DISTANCE") ? station.get("DISTANCE").asDouble() : null)
                        .coordinateAccuracyValidated(isValid)
                        .build();

                stations.add(stationInfo);
            }

            log.info("주유소 좌표 변환 완료 - 총 {}개 중 {}개 변환 오차 발생",
                    stations.size(), conversionErrors);

            // 응답 DTO 생성
            return StationResponse.builder()
                    .stations(stations)
                    .coordinateSystem("WGS84")
                    .originalCoordinateSystem("TM128")
                    .conversionErrors(conversionErrors)
                    .conversionPrecisionMeters(0.01)
                    .build();

        } catch (Exception e) {
            log.error("주유소 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("주유소 목록 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 주유소 상세 정보 조회 (원본 데이터 그대로 반환)
     */
    public Map<String, Object> getStationDetails(String stationId) {
        try {
            // Opinet API 호출
            String url = buildStationDetailsUrl(stationId);
            log.info("주유소 상세 정보 요청: stationId={}", stationId);

            String json = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = objectMapper.readValue(json, Map.class);

            return result;

        } catch (Exception e) {
            log.error("주유소 상세 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("주유소 상세 정보 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 주변 주유소 URL 생성
     */
    private String buildNearbyStationsUrl(double x, double y, String sort, int radius) {
        return "http://www.opinet.co.kr/api/aroundAll.do?" +
                "code=" + apiKey +
                "&x=" + x +
                "&y=" + y +
                "&radius=" + radius +
                "&sort=" + sort +
                "&prodcd=B027&out=json";
    }

    /**
     * 주유소 상세 정보 URL 생성
     */
    private String buildStationDetailsUrl(String stationId) {
        return "http://www.opinet.co.kr/api/detailById.do?" +
                "code=" + apiKey +
                "&id=" + stationId +
                "&out=json";
    }
}

