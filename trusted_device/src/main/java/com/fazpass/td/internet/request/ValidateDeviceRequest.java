package com.fazpass.td.internet.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ValidateDeviceRequest {

    public ValidateDeviceRequest(String userId, String device, String packageName, String meta, String key, String timeZone, List<EnrollDeviceRequest.Contact> contacts, EnrollDeviceRequest.Location location, List<EnrollDeviceRequest.Sim> sims) {
        this.userId = userId;
        this.device = device;
        this.packageName = packageName;
        this.meta = meta;
        this.key = key;
        this.timeZone = timeZone;
        this.contacts = contacts;
        this.location = location;
        this.sims = sims;
    }

    @SerializedName("user_id")
    private String userId;

    @SerializedName("device")
    private String device;

    @SerializedName("app")
    private String packageName;

    @SerializedName("meta")
    private String meta;

    @SerializedName("key")
    private String key;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("contacts")
    private List<EnrollDeviceRequest.Contact> contacts;

    @SerializedName("location")
    private EnrollDeviceRequest.Location location;

    @SerializedName("sim")
    private List<EnrollDeviceRequest.Sim> sims;

    public static class Sim{
        @SerializedName("serial")
        private String serialNumber;
        @SerializedName("phone")
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

    public static class Contact {
        @SerializedName("name")
        private String name;
        @SerializedName("phone")
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

    public static class Location {
        @SerializedName("lat")
        private String latitude;
        @SerializedName("lng")
        private String longitude;

        public Location(double latitude, double longitude) {
            this.latitude = String.valueOf(latitude);
            this.longitude = String.valueOf(longitude);
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getDevice() {
        return device;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getMeta() {
        return meta;
    }

    public String getKey() {
        return key;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public List<EnrollDeviceRequest.Contact> getContacts() {
        return contacts;
    }

    public EnrollDeviceRequest.Location getLocation() {
        return location;
    }

    public List<EnrollDeviceRequest.Sim> getSims() {
        return sims;
    }
}
