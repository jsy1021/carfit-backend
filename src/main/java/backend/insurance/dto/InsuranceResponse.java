package backend.insurance.dto;

import lombok.Data;

@Data
public class InsuranceResponse {
    private int insuranceFee;
    private InsuranceDetails details;

    public InsuranceResponse(int insuranceFee, InsuranceDetails details) {
        this.insuranceFee = insuranceFee;
        this.details = details;
    }

    @Data
    public static class InsuranceDetails {
        private int baseInsurance;
        private Surcharges surcharges;
        private double totalDiscount;

        @Data
        public static class Surcharges {
            private double carType;
            private double license;
            private double accident;
            private double drunkDriving;
            private double trafficViolation;
            private double region;
        }
    }
}
