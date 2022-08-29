package com.fazpass.td;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import at.favre.lib.crypto.bcrypt.BCrypt;


class FazpassKey {
    private final Context context;
    private String userId;
    private String meta;
    private final String pin;
    private final User user;
    private final Device device;

    public FazpassKey(Context context, User user, Device device, String pin) {
        this.context = context;
        this.pin = BCrypt.withDefaults().hashToString(12, pin.toCharArray());;
        this.user = user;
        this.device = device;
        initialize();
    }

    private void initialize(){
        try{
            userId = UUID.randomUUID().toString();
            Storage.storeDataLocal(context, TD.PUBLIC_KEY, userId);
            String keyStoreAlias = generateKeyAlias(userId);
            String password = BCrypt.withDefaults().hashToString(12, userId.toCharArray());
            Storage.storeDataLocal(context, TD.PRIVATE_KEY,password);
            meta = Crypto.encrypt(keyStoreAlias,password);
            Storage.storeDataLocal(context, TD.META,meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String generateKeyAlias(String uuid){
        JSONObject json = new JSONObject();
        try{
            json.put(TD.MERCHANT_TOKEN,Merchant.merchantToken);
            json.put(TD.PACKAGE_NAME, context.getPackageName());
            json.put(TD.PUBLIC_KEY, uuid);
            json.put(TD.USER_PHONE, user.getPhone());
            json.put(TD.USER_EMAIL, user.getEmail());
            json.put(TD.USER_PIN, pin);
            json.put(TD.DEVICE, device.getDevice());
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return json.toString();
        }

    }
    public String getUserId() {
        return userId;
    }

    public String getMeta() {
        return meta;
    }

    public String getPin() {
        return pin;
    }
}
