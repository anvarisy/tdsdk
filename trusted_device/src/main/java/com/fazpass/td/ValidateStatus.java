package com.fazpass.td;

public class ValidateStatus {
    private final boolean status;
    private final Confidence confidenceRate;

    public ValidateStatus(boolean status, Confidence confidenceRate) {
        this.status = status;
        this.confidenceRate = confidenceRate;
    }

    public boolean isStatus() {
        return status;
    }

    public Confidence getConfidenceRate() {
        return confidenceRate;
    }

    public static class Confidence{
        private double meta;
        private double key;
        private double sim;
        private double contact;
        private double location;

        public Confidence(double meta, double key, double sim, double contact, double location) {
            this.meta = meta;
            this.key = key;
            this.sim = sim;
            this.contact = contact;
            this.location = location;
        }

        public double getMeta() {
            return meta;
        }

        public double getKey() {
            return key;
        }

        public double getSim() {
            return sim;
        }

        public double getContact() {
            return contact;
        }

        public double getLocation() {
            return location;
        }
    }
}
