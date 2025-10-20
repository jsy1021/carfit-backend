package backend.oilprice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SidoApiResponse {
    @JsonProperty("RESULT")
    private Result result;

    @Data
    public static class Result {
        @JsonProperty("OIL")
        private List<SidoOilData> oil;
    }

    @Data
    public static class SidoOilData {
        @JsonProperty("SIDOCD")
        private String sidoCd;
        
        @JsonProperty("SIDONM")
        private String sidoNm;
        
        @JsonProperty("PRODCD")
        private String prodcd;
        
        @JsonProperty("PRICE")
        private Double price;
        
        @JsonProperty("DIFF")
        private Double diff;
    }
}
