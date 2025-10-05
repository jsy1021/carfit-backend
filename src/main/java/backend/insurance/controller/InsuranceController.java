package backend.insurance.controller;

import backend.insurance.dto.InsuranceRequestDto;
import backend.insurance.dto.InsuranceResponseDto;
import backend.insurance.service.InsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

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