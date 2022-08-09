package com.fazpass.td.internet.request;

import com.google.gson.annotations.SerializedName;

public class CheckUserRequest {

    public CheckUserRequest(String emailOrPhone, String app, String device) {
        if(emailOrPhone.equals("")) throw new NullPointerException("user id cannot be null or empty");
        this.emailOrPhone = emailOrPhone;
        this.app = app;
        this.device = device;
    }

    @SerializedName("email_phone")
    private String emailOrPhone;

    // This is package name
    @SerializedName("app")
    private String app;

    // This is device meta
    @SerializedName("device")
    private String device;

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public String getApp() {
        return app;
    }

    public String getDevice() {
        return device;
    }
}
