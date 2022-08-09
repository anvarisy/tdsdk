package com.fazpass.td;

public class Sim {
    private String serialNumber;
    private String phoneNumber;

    public Sim(String serialNumber, String phoneNumber) {
        this.serialNumber = serialNumber;
        this.phoneNumber = phoneNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
