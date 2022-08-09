package com.fazpass.td;

import androidx.annotation.NonNull;

public class User {
    public static String userId;
    public static String emailOrMobile;

    private String email;
    private String phone;
    private String name;
    private String idCard;
    private String address;

    public User(@NonNull String email, @NonNull String phone, @NonNull String name, @NonNull String idCard, @NonNull String address) {
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
