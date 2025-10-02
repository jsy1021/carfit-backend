package backend.insurance.dto;

import lombok.Data;



@Data
public class InsuranceDetailsDto {
    private Long baseInsurance;
    private Double driverRisk;
    private Double regionRisk;
    private Double totalDiscount;

}
