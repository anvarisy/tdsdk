package com.fazpass.td.internet.response;

import com.google.gson.annotations.SerializedName;

public class CheckUserResponse {

    @SerializedName("id")
    private String userId;

    @SerializedName("key")
    private String key;

    public CheckUserResponse(String userId, String key) {
        this.userId = userId;
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public String getKey() {
        return key;
    }
}
