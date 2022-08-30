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
            Storage.storeDataLocal(context, BASE.PUBLIC_KEY, userId);
            String keyStoreAlias = generateKeyAlias(userId);
            String password = BCrypt.withDefaults().hashToString(12, userId.toCharArray());
            Storage.storeDataLocal(context, BASE.PRIVATE_KEY,password);
            meta = Crypto.encrypt(keyStoreAlias,password);
            Storage.storeDataLocal(context, BASE.META,meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String generateKeyAlias(String uuid){
        JSONObject json = new JSONObject();
        try{
            json.put(BASE.MERCHANT_TOKEN,Merchant.merchantToken);
            json.put(BASE.PACKAGE_NAME, context.getPackageName());
            json.put(BASE.PUBLIC_KEY, uuid);
            json.put(BASE.USER_PHONE, user.getPhone());
            json.put(BASE.USER_EMAIL, user.getEmail());
            json.put(BASE.USER_PIN, pin);
            json.put(BASE.DEVICE, device.getDevice());
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
