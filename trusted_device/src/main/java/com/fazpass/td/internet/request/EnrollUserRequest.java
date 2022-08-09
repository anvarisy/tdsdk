package com.fazpass.td.internet.request;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class EnrollUserRequest {
    @SerializedName("email")
    private String email;
    @SerializedName("phone")
    private String phone;
    @SerializedName("name")
    private String name;
    @SerializedName("ktp")
    private String idCard;
    @SerializedName("address")
    private String address;

    public EnrollUserRequest(@NonNull String email, @NonNull String phone, @NonNull String name, @NonNull String idCard, @NonNull String address) {
        if(email.equals("") || phone.equals("")){
            throw new ExceptionInInitializerError("email or phone is required");
        }
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.idCard = idCard;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getIdCard() {
        return idCard;
    }

    public String getAddress() {
        return address;
    }
}
