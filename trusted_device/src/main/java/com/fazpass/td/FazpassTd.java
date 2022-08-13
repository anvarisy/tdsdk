package com.fazpass.td;


import android.util.Log;
import android.webkit.URLUtil;

import androidx.appcompat.app.AppCompatActivity;

import com.fazpass.td.internet.request.CheckUserRequest;
import com.fazpass.td.internet.Roaming;
import com.fazpass.td.internet.UseCase;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class FazpassTd {
    private final AppCompatActivity context;

    public FazpassTd(AppCompatActivity context, String merchantToken, String packageName, String baseUrl) {
        Device device = new Device(context);
        if (!context.getPackageName().equals(packageName)){
            Log.e(Fazpass.TAG, String.format("your current package is %s that not match with %s",
                    context.getPackageName(),packageName));
            throw new SecurityException("package name not match");
        }
        else if (merchantToken == null || merchantToken.equals("")){
            throw new NullPointerException("merchant id cannot be null or empty");
        }
        else if (baseUrl == null || baseUrl.equals("") || !URLUtil.isValidUrl(baseUrl)){
            throw new NullPointerException("base URL should be valid");
        }else if (device.isRooted() || device.isEmulator()){
            throw new SecurityException("Device rooted or is an emulator");
        }
        Merchant.merchantToken = merchantToken;
        Merchant.baseUrl = baseUrl;
        this.context = context;

    }

    public Observable<TrustedDevice> initialize(String email, String phone){
        String packageName = context.getPackageName();
        Device device = new Device(context);
        GeoLocation location = new GeoLocation(context);
        CheckUserRequest.Location locationDetail = new CheckUserRequest.Location(location.getLatitude(),location.getLongitude());
        CheckUserRequest body = new CheckUserRequest(email,phone, packageName,device.getDevice(), location.getTimezone(),locationDetail);
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.startService(Merchant.merchantToken,body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resp->{
//                               subscriber.onNext(new TrustedDevice(context));
                                subscriber.onNext(new TrustedDevice(context,
                                        Helper.getStatusPhone(context,resp.getStatus(),resp.getCode()),
                                        resp.getData()));
                                subscriber.onComplete();
                            },err->{
                                subscriber.onError(err);
                                Log.e(Fazpass.TAG,err.getMessage());
                            }
                    );
        });

    }


}
