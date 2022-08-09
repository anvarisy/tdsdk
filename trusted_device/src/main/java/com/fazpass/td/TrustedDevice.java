package com.fazpass.td;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fazpass.td.internet.response.CheckUserResponse;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TrustedDevice {
    private Context context;
    private int status;
    public static final int KEY_LOCALE_NOT_FOUND = 0;
    public static final int KEY_SERVER_NOT_FOUND = 2;
    public static final int KEY_READY_TO_COMPARE = 1;
    public static final int KEY_NOT_MATCH = 3;
    public static final int KEY_IS_MATCH = 4;
    public static final int USER_NOT_FOUND = 5;
    public int getStatus() {
        return status;
    }

    TrustedDevice(Context context, int st, CheckUserResponse resp) {
        if(!Objects.equals(st, KEY_READY_TO_COMPARE)){
           this.status = st;
        }else{
            String stringKey = Storage.readDataLocal(context,Fazpass.PRIVATE_KEY);
            byte[] rawKey = Base64.getDecoder().decode(stringKey);
            SecretKey key = new SecretKeySpec(rawKey, 0, rawKey.length, Fazpass.AES);
            try{
                String alias = Key.decrypt(resp.getKey(),key, Key.generateIv(resp.getUserId().substring(0,16)));
                if(!Key.generateKeyStoreAlias(Merchant.merchantToken, context,resp.getUserId(), User.emailOrMobile, Key.generateAppId(context)).equals(alias)){
                    this.status = KEY_NOT_MATCH;
                }else{
                    this.status = KEY_IS_MATCH;
                    //TODO update last active
                }
            } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                e.printStackTrace();
                this.status = KEY_NOT_MATCH;
            }
        }
        this.context = context;
    }

    public void enrollDeviceByPin(User user, @NonNull String pin){

    }

/*    public void validateUser(String identity, SetOnValidateUser cb){
        UseCase u = Roaming.start();
        u.startService(Merchant.merchantToken,new CheckUserBody(identity))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp->{
                    User.userId = resp.getUserId();
                    Phone phone = new Phone(context);
                    Key key = new Key(context);
                    cb.onComplete(new TrustedDevice(phone,key));
                },err->{
                    cb.onError(err);
                });

    }

    public void enrollDeviceByPin(String pin, Callback<EnrollDeviceResponse> response){
        UseCase useCase = Roaming.start();
        useCase.enrollDevice(Merchant.merchantToken,new EnrollDeviceBody("",new String[]{""},""))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response::onDeviceEnrolled, Throwable::printStackTrace);;
    }*/
}
