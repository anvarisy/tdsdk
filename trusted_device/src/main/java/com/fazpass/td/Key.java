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


class Key {
    private final Context context;
    private String keyStoreAlias;
    private String userId;
    private String meta;
    private String pin;
    private User user;
    private Device device;
    public Key(Context context, User user, Device device, String pin) {
        this.context = context;
        this.pin = BCrypt.withDefaults().hashToString(12, pin.toCharArray());;
        this.user = user;
        this.device = device;
        initialize();
    }

    private void initialize(){
        try{
            userId = UUID.randomUUID().toString();
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
/*
    private SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(Fazpass.AES);
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    static SecretKey generatePrivetKey(String userId){
        try{
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            // Before the keystore can be accessed, it must be loaded.
            keyStore.load(null);
            SecretKey privateKey = (SecretKey) keyStore.getKey(userId, null);
            return privateKey;
        }catch (Exception e){
            return null;
        }

    }

    @NonNull
    static IvParameterSpec generateIv(String uuid) {
        byte[] iv = uuid.substring(0,16).getBytes(StandardCharsets.UTF_8);
        return new IvParameterSpec(iv);
    }

    @NonNull
    private SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
    }

    static String encrypt(@NonNull String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Fazpass.AES_CBC_PKCS_5_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] publicKey = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(publicKey);
    }

    static String decrypt(String publicKey, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Fazpass.AES_CBC_PKCS_5_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(publicKey));
        return new String(plainText);
    }
*/
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
