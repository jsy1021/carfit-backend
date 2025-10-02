package backend.insurance.controller;

import backend.insurance.dto.InsuranceRequestDto;
import backend.insurance.dto.InsuranceResponseDto;
import backend.insurance.service.InsuranceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class InsuranceController {

    private final InsuranceService insuranceService;

    @Autowired
    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @PostMapping("/calculate-insurance")
    public ResponseEntity<InsuranceResponseDto> calculateInsurance(@RequestBody InsuranceRequestDto request) {
        try {
            InsuranceResponseDto response = insuranceService.calculateInsurance(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("보험료 계산 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}