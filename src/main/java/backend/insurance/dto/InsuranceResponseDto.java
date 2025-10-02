package backend.insurance.dto;

import lombok.Data;

@Data
public class InsuranceResponseDto {
    private Long insuranceFee;
    private InsuranceDetailsDto details;
}



