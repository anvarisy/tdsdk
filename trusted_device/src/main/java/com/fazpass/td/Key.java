package com.fazpass.td;

import android.annotation.SuppressLint;
import android.content.Context;

import android.provider.Settings;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

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

public class Key {
    private final String applicationId;
    private final Context context;
    private final String keyStoreAlias;
    private String publicKey;

    public String getApplicationId() {
        return applicationId;
    }

    public Key(Context context) {
        this.context = context;
        this.applicationId = generateAppId();
        this.keyStoreAlias = generateKeyStoreAlias();
    }

    @SuppressLint("HardwareIds")
    private String generateAppId(){
        String appId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Storage.storeToLocal(context,Fazpass.APP_ID,appId);
        return appId;
    }

    @NonNull
    private String generateKeyStoreAlias(){
        String alias = Merchant.merchantToken +","+context.getPackageName()+","+User.userId+","+User.emailOrMobile+","+generateAppId();
        Storage.storeToLocal(context,Fazpass.KEY_ALIAS,alias);
        return alias;
    }

    static String generateKeyStoreAlias(String merchantToken, Context context, String userId, String userEmailPhone, String appId){
        return merchantToken +","+context.getPackageName()+","+userId+","+userEmailPhone+","+appId;
    }

    @SuppressLint("HardwareIds")
    static String generateAppId(Context context){
        return Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(Fazpass.AES);
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        String privateKey = Base64.getEncoder().encodeToString(key.getEncoded());
        Storage.storeToLocal(context,Fazpass.PRIVATE_KEY,privateKey);
        return key;
    }

    @NonNull
    static IvParameterSpec generateIv(String uuid) {
        if(uuid.length() < 16){
            throw new ArrayIndexOutOfBoundsException();
        }
        String id = uuid.substring(0,16);
        byte[] iv = id.getBytes(StandardCharsets.UTF_8);
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

}
