package backend.insurance.service;

import backend.insurance.dto.InsuranceRequestDto;
import backend.insurance.dto.InsuranceResponseDto;
import backend.insurance.dto.InsuranceDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class InsuranceService {

    // 연령대별 기본 보험료 계산
    private long calculateAgeBaseInsurance(Integer age) {
        if (age < 21) return 2500000;      // 18-20세
        else if (age < 25) return 2000000; // 21-24세
        else if (age < 30) return 1600000; // 25-29세
        else if (age < 40) return 1200000; // 30-39세
        else if (age < 50) return 1000000; // 40-49세
        else if (age < 60) return 900000;  // 50-59세
        else return 1000000;               // 60세 이상
    }

    // 차량 요율 계산
    private double calculateCarFactor(String carType, Long carPrice, Integer engineSize) {
        double factor = 1.0;

        // 차종별 요율
        switch(carType) {
            case "경차":
                factor *= 0.7;  // 30% 할인
                break;
            case "소형차":
                factor *= 0.85; // 15% 할인
                break;
            case "중형차":
                factor *= 1.0;  // 기본 요율
                break;
            case "대형차":
                factor *= 1.2;  // 20% 할증
                break;
            case "SUV":
                factor *= 1.2;  // 20% 할증
                break;
            case "트럭":
                factor *= 1.5;  // 50% 할증
                break;
        }

        // 배기량별 요율
        if (engineSize >= 3000) factor *= 1.3;      // 3000cc 이상: 30% 할증
        else if (engineSize >= 2000) factor *= 1.2; // 2000cc 이상: 20% 할증
        else if (engineSize >= 1600) factor *= 1.1; // 1600cc 이상: 10% 할증

        // 차량 가격별 요율
        if (carPrice >= 50000000) factor *= 1.3;      // 5천만원 이상: 30% 할증
        else if (carPrice >= 30000000) factor *= 1.2; // 3천만원 이상: 20% 할증
        else if (carPrice >= 20000000) factor *= 1.1; // 2천만원 이상: 10% 할증

        return factor;
    }

    // 운전자 요율 계산
    private double calculateDriverFactor(Integer age, String gender, Integer licenseYear,
                                         Integer accidentCount, Integer trafficViolations,
                                         Boolean drunkDriving) {
        double factor = 1.0;

        // 연령별 요율
        if (age < 21) factor *= 1.3;      // 21세 미만: 30% 할증
        else if (age < 25) factor *= 1.1; // 21-24세: 10% 할증
        else if (age < 30) factor *= 1.05; // 25-29세: 5% 할증
        else if (age >= 65) factor *= 1.2; // 65세 이상: 20% 할증

        // 성별 요율 (일부 보험사 적용)
        if ("MALE".equals(gender)) factor *= 1.03; // 남성: 3% 할증

        // 운전경력별 요율
        if (licenseYear < 1) factor *= 1.3;     // 1년 미만: 30% 할증
        else if (licenseYear < 3) factor *= 1.15; // 1-2년: 15% 할증
        else if (licenseYear >= 10) factor *= 0.9; // 10년 이상: 10% 할인

        // 사고이력별 요율
        if (accidentCount > 0) factor *= 1.05; // 사고 있음: 5% 할증

        // 위반이력별 요율
        factor *= (1 + trafficViolations * 0.05); // 위반 1건당 5% 할증

        // 음주운전 이력
        if (drunkDriving) factor *= 1.5; // 음주운전: 50% 할증

        return factor;
    }

    // 지역별 요율 계산
    private double calculateRegionFactor(String region) {
        Map<String, Double> regionFactors = new HashMap<>();
        regionFactors.put("서울", 1.2);
        regionFactors.put("부산", 1.15);
        regionFactors.put("인천", 1.1);
        regionFactors.put("대구", 1.1);
        regionFactors.put("광주", 1.1);
        regionFactors.put("대전", 1.05);
        regionFactors.put("울산", 1.05);
        regionFactors.put("세종", 1.0);
        regionFactors.put("경기", 1.15);
        regionFactors.put("강원", 0.95);
        regionFactors.put("충북", 0.95);
        regionFactors.put("충남", 0.95);
        regionFactors.put("전북", 0.9);
        regionFactors.put("전남", 0.9);
        regionFactors.put("경북", 0.9);
        regionFactors.put("경남", 0.95);
        regionFactors.put("제주", 0.85);

        return regionFactors.getOrDefault(region, 1.0);
    }


    // ... existing code ...
    // 할인율 계산
    private double calculateDiscounts(InsuranceRequestDto request) {
        double totalDiscount = 0;

        // 무사고 할인 (최근 3년 무사고)
        if (request.getAccidentCount() != null && request.getAccidentCount() == 0) {
            totalDiscount += 0.15; // 15% 할인
        }

        // 마일리지 할인 (연간 1만km 이하)
        if (request.getAnnualMileage() != null && request.getAnnualMileage() <= 10000) {
            totalDiscount += 0.10; // 10% 할인
        }

        // 블랙박스 장착 할인
        if (request.getOptions() != null && Boolean.TRUE.equals(request.getOptions().getBlackBox())) {
            totalDiscount += 0.033; // 3.3% 할인
        }

        // 자녀 할인 (만 6세 이하 자녀 1명)
        if (request.getOptions() != null && Boolean.TRUE.equals(request.getOptions().getHasChild())) {
            totalDiscount += 0.07; // 7% 할인
        }

        // 다자녀 할인 (자녀 2명 이상)
        if (request.getOptions() != null && Boolean.TRUE.equals(request.getOptions().getHasMultipleChildren())) {
            totalDiscount += 0.04; // 4% 할인
        }

        // 안전장치 장착 할인 (ADAS)
        if (request.getOptions() != null && Boolean.TRUE.equals(request.getOptions().getAdas())) {
            totalDiscount += 0.042; // 4.2% 할인
        }

        // 인터넷 가입 할인
        if (Boolean.TRUE.equals(request.getIsDirect())) {
            totalDiscount += 0.10; // 10% 할인
        }

        // 군 운전병 할인
        if (request.getOptions() != null && Boolean.TRUE.equals(request.getOptions().getIsMilitaryDriver())) {
            totalDiscount += 0.04; // 4% 할인
        }

        // 대중교통 할인
        if (request.getOptions() != null && Boolean.TRUE.equals(request.getOptions().getUsesPublicTransport())) {
            totalDiscount += 0.04; // 4% 할인
        }

        // 최대 할인율 40%로 제한 (실제 보험사 기준)
        return Math.min(totalDiscount, 0.4);
    }


    // ... existing code ...
    public InsuranceResponseDto calculateInsurance(InsuranceRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("보험료 계산 요청이 null입니다.");
        }

        // 필수 필드 검증
        if (request.getAge() == null || request.getCarType() == null ||
                request.getCarPrice() == null || request.getEngineSize() == null ||
                request.getCarYear() == null || request.getAnnualMileage() == null ||
                request.getLicenseYear() == null || request.getRegion() == null) {
            throw new IllegalArgumentException("필수 입력 항목이 누락되었습니다.");
        }

        // 연령대별 기본 보험료 계산
        long ageBaseInsurance = calculateAgeBaseInsurance(request.getAge());

        // 차량 요율 계산
        double carFactor = calculateCarFactor(
                request.getCarType(),
                request.getCarPrice(),
                request.getEngineSize()
        );

        // 운전자 요율 계산
        double driverFactor = calculateDriverFactor(
                request.getAge(),
                request.getGender() != null ? request.getGender() : "MALE",
                request.getLicenseYear(),
                request.getAccidentCount() != null ? request.getAccidentCount() : 0,
                request.getTrafficViolations() != null ? request.getTrafficViolations() : 0,
                Boolean.TRUE.equals(request.getDrunkDriving())
        );

        // 지역별 요율 계산
        double regionFactor = calculateRegionFactor(request.getRegion());

        // 할인율 계산
        double totalDiscount = calculateDiscounts(request);

        // 최종 보험료 계산
        long finalInsurance = Math.round(ageBaseInsurance * carFactor * driverFactor * regionFactor * (1 - totalDiscount));

        // 응답 생성
        InsuranceResponseDto response = new InsuranceResponseDto();
        response.setInsuranceFee(finalInsurance);

        InsuranceDetailsDto details = new InsuranceDetailsDto();
        details.setBaseInsurance(ageBaseInsurance);
        details.setDriverRisk((driverFactor - 1) * 100);
        details.setRegionRisk((regionFactor - 1) * 100);
        details.setTotalDiscount(totalDiscount * 100);

        response.setDetails(details);

        return response;
    }
// ... existing code ...
}