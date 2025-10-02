package backend.insurance.dto;

import lombok.Data;

@Data
public class InsuranceRequest {
    private int age;
    private String carType;
    private int carPrice;
    private int carYear;
    private int licenseYear;
    private int accidentCount;
    private boolean drunkDriving;
    private int trafficViolations;
    private String region;
    private boolean isDirect;
    private InsuranceOptions options;

    @Data
    public static class InsuranceOptions {
        private boolean blackBox;
        private boolean mileage;
        private boolean driverScopeLimited;
        private boolean adas;
        private boolean safeDriving;
        private boolean groupDiscount;
        private boolean jobDiscount;
    }
}