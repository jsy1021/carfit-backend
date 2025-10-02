package backend.insurance.dto;

import lombok.Data;

@Data
public class InsuranceRequestDto {
    private Integer age;
    private String gender;  // "MALE" or "FEMALE"
    private String carType;
    private Long carPrice;
    private Integer carYear;
    private Integer engineSize;  // 배기량 (cc)
    private Integer licenseYear;
    private Integer accidentCount;
    private Boolean drunkDriving;
    private Integer trafficViolations;
    private String region;
    private Boolean isDirect;
    private Integer annualMileage;  // 연간 주행거리 (km)
    private InsuranceOptionsDto options;
}
