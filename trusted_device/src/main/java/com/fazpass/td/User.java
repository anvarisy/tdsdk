package com.fazpass.td;

import androidx.annotation.NonNull;

public class User {
    private final String email;
    private final String phone;
    private final String name;
    private final String idCard;
    private final String address;

    public User(@NonNull String email, @NonNull String phone, @NonNull String name, @NonNull String idCard, @NonNull String address) {
        if(email.equals("") && phone.equals("")){
            throw new NullPointerException("email or phone is required");
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
