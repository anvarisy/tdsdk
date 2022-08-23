package com.fazpass.td;

public abstract class Fazpass {
    static final String DEBUG = "";
    static final String STAGING = "";
    static final String PRODUCTION = "";
    static final String MERCHANT_TOKEN = "merchant_token";
    static final String PACKAGE_NAME = "package_name";
    static final String PRIVATE_KEY = "private_key";
    static final String PUBLIC_KEY = "public_key";
    static final String USER_EMAIL = "user_email";
    static final String USER_PHONE = "user_phone";
    static final String USER_PIN = "user_pin";
    static final String USER_ID = "user_id";
    static final String DEVICE = "device";
    static final String META = "meta";
    static final String TAG = "fazpass";

    static final String LOCAL_MISSING = "LOCAL DATA IS MISSING";
    static final String PIN_NOT_MATCH = "COMPARING PIN IS FAILED";
/*  static final String IV = "iv";
    static final String FINGER_PASSWORD = "finger_password";
    static final String KEY_ALIAS = "key_alias";
    static final String DEVICE_ROOTED = "device_rooted";
    static final String DEVICE_CONTACTS = "device_contact";
    static final String DEVICE_SIMS = "device_sims";*/
/*    static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
    static final String AES = "AES";*/
    public static class Sim{
        public static final String CARDS = "cards";
        public static final String CARRIER_NAME = "carrier_name";
        public static final String COUNTRY_CODE = "country_code";
        public static final String DISPLAY_NAME = "display_name";
        public static final String IS_DATA_ROAMING = "is_data_roaming";
        public static final String IS_NETWORK_ROAMING = "is_network_roaming";
        public static final String SUBSCRIPTION_ID = "subscription_id";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String SERIAL_NUMBER = "serial_number";
    }
}
