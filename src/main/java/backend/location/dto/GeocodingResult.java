package backend.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocodingResult {
    private double latitude;
    private double longitude;
    private String roadAddress;
    private String jibunAddress;
    private boolean success;
}
