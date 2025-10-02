package backend.insurance.dto;

import lombok.Data;



@Data
public class InsuranceOptionsDto {
    private Boolean blackBox;        // 블랙박스 장착
    private Boolean hasChild;        // 만 6세 이하 자녀 1명
    private Boolean hasMultipleChildren; // 자녀 2명 이상
    private Boolean adas;            // ADAS 장착
    private Boolean isMilitaryDriver; // 군 운전병
    private Boolean usesPublicTransport; // 대중교통 이용
}
