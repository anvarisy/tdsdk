package com.fazpass.td;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.google.firebase.messaging.FirebaseMessaging;
import com.scottyab.rootbeer.RootBeer;

import io.reactivex.rxjava3.core.Observable;


class Device {
    private final Context context;
    private String device;
    private String notificationToken;

    public Device(Context context) {
        this.context = context;
        initialize(context);
        readNotificationToken().subscribe(s->{
            this.notificationToken = s;
        });
    }

    private void initialize(Context context){
        this.device = readMeta()+"-"+generateAppId(context);
    }

    @SuppressLint("HardwareIds")
    private String generateAppId(Context context){
        return Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String readMeta(){
        String meta = ""+ Build.BRAND+","+Build.MODEL+","+Build.VERSION.SDK_INT;
        return meta;
    }


    boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    boolean isRooted() {
        RootBeer rootBeer = new RootBeer(context);
        return rootBeer.isRooted();
    }

    String getDevice() {
        return device;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    private Observable<String> readNotificationToken(){
        return Observable.create(subscriber->{
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s->{
                subscriber.onNext(s);
                subscriber.onComplete();
            }).addOnFailureListener(f->{
                subscriber.onError(f);
            }).addOnCompleteListener(a->{

            });
        });
    }
}
