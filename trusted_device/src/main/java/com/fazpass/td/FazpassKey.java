package com.fazpass.td;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import at.favre.lib.crypto.bcrypt.BCrypt;


class FazpassKey {
    private final Context context;
    private String keyStoreAlias;
    private String userId;
    private String meta;
    private String pin;
    private User user;
    private Device device;
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
            if(Storage.isDataExists(context,Fazpass.USER_ID)){
                Storage.removeDataLocal(context,Fazpass.USER_ID);
            }
            Storage.storeToLocal(context,Fazpass.USER_ID, userId);
            keyStoreAlias = generateKeyAlias(userId);
            String password = BCrypt.withDefaults().hashToString(12, userId.toCharArray());
            if(Storage.isDataExists(context,Fazpass.PRIVATE_KEY)){
                Storage.removeDataLocal(context,Fazpass.PRIVATE_KEY);
            }
            Storage.storeToLocal(context,Fazpass.PRIVATE_KEY,password);
            meta = Crypto.encrypt(keyStoreAlias,password);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String generateKeyAlias(String uuid){
        JSONObject json = new JSONObject();
        try{
            json.put(Fazpass.MERCHANT_TOKEN,Merchant.merchantToken);
            json.put(Fazpass.PACKAGE_NAME, context.getPackageName());
            json.put(Fazpass.PUBLIC_KEY, uuid);
            json.put(Fazpass.USER_PHONE, user.getPhone());
            json.put(Fazpass.USER_EMAIL, user.getEmail());
            json.put(Fazpass.USER_PIN, pin);
            json.put(Fazpass.DEVICE, device.getDevice());
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
