package com.fazpass.td.internet.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EnrollDeviceRequest {

    @SerializedName("public_key")
    private String publicKey;

    @SerializedName("pin")
    private String pin;

    @SerializedName("contact")
    private List<Contact> contacts;

    @SerializedName("sims")
    private List<Sim> sims;


}


class Contact{
    @SerializedName("name")
    private String name;
    @SerializedName("phone_number")
    private List<String> phoneNumber;

    public Contact(String name, List<String> phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public List<String> getPhoneNumber() {
        return phoneNumber;
    }
}

class Sim{
    @SerializedName("serial_number")
    private String serialNumber;
    @SerializedName("phone_number")
    private String phoneNumber;

    public Sim(String serialNumber, String phoneNumber) {
        this.serialNumber = serialNumber;
        this.phoneNumber = phoneNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}