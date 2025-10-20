package backend.oilprice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SigunguApiResponse {
    @JsonProperty("RESULT")
    private Result result;

    @Data
    public static class Result {
        @JsonProperty("OIL")
        private List<SigunguOilData> oil;
    }

    @Data
    public static class SigunguOilData {
        @JsonProperty("SIGUNCD")
        private String sigunCd;
        
        @JsonProperty("SIGUNNM")
        private String sigunNm;
        
        @JsonProperty("PRODCD")
        private String prodcd;
        
        @JsonProperty("PRICE")
        private Double price;
        
        @JsonProperty("DIFF")
        private Double diff;
    }
}
