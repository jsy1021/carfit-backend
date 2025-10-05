package backend.station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationResponse {
    
    private List<StationInfo> stations;
    
    @JsonProperty("coordinate_system")
    private String coordinateSystem;
    
    @JsonProperty("original_coordinate_system")
    private String originalCoordinateSystem;
    
    @JsonProperty("conversion_errors")
    private Integer conversionErrors;
    
    @JsonProperty("conversion_precision_meters")
    private Double conversionPrecisionMeters;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationInfo {
        @JsonProperty("UNI_ID")
        private String id;
        
        @JsonProperty("OS_NM")
        private String name;
        
        @JsonProperty("POLL_DIV_CD")
        private String brand;
        
        @JsonProperty("NEW_ADR")
        private String address;
        
        @JsonProperty("TEL")
        private String tel;
        
        @JsonProperty("PRICE")
        private String price;
        
        @JsonProperty("GIS_Y_COOR")  // 위도 (WGS84)
        private Double latitude;
        
        @JsonProperty("GIS_X_COOR")  // 경도 (WGS84)
        private Double longitude;
        
        @JsonProperty("DISTANCE")
        private Double distance;
        
        @JsonProperty("coordinate_accuracy_validated")
        private Boolean coordinateAccuracyValidated;
    }
}

