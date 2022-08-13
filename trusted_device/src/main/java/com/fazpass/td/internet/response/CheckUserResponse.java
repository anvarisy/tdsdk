package com.fazpass.td.internet.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CheckUserResponse {
    public CheckUserResponse(User user, Apps apps) {
        this.user = user;
        this.apps = apps;
    }

    @SerializedName("user")
    private User user;

    @SerializedName("apps")
    private Apps apps;

    public User getUser() {
        return user;
    }

    public Apps getApps() {
        return apps;
    }

    public class Apps{
        @SerializedName("current")
        private App current;
        @SerializedName("others")
        private List<App> others;

        public Apps(App current, List<App> others) {
            this.current = current;
            this.others = others;
        }

        public App getCurrent() {
            return current;
        }

        public List<App> getOthers() {
            return others;
        }
    }

    public class User{

        public User(String id) {
            this.id = id;
        }
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }

    }

    public class App{
        public App(String meta, String key, boolean trusted, boolean use_fingerprint, boolean use_pin, String device) {
            this.meta = meta;
            this.key = key;
            this.trusted = trusted;
            this.use_fingerprint = use_fingerprint;
            this.use_pin = use_pin;
            this.device = device;
        }
        //Hashed information
        @SerializedName("meta")
        private String meta;

        //16 character string for unlock meta
        @SerializedName("key")
        private String key;

        @SerializedName("is_trusted")
        private boolean trusted;

        @SerializedName("use_fingerprint")
        private boolean use_fingerprint;

        @SerializedName("use_pin")
        private boolean use_pin;

        @SerializedName("device")
        private String device;

        public String getMeta() {
            return meta;
        }

        public String getKey() {
            return key;
        }

        public boolean isTrusted() {
            return trusted;
        }

        public boolean isUse_fingerprint() {
            return use_fingerprint;
        }

        public boolean isUse_pin() {
            return use_pin;
        }

        public String getDevice() {
            return device;
        }
    }
}



