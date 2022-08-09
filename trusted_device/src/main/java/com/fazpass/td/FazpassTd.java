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
        if (!context.getPackageName().equals(packageName)){
            Log.e(Cons.TAG, String.format("your current package is %s that not match with %s",
                    context.getPackageName(),packageName));
            throw new SecurityException("package name not match");
        }
        else if (merchantToken == null || merchantToken.equals("")){
            throw new NullPointerException("merchant id cannot be null or empty");
        }
        else if (baseUrl == null || baseUrl.equals("") || !URLUtil.isValidUrl(baseUrl)){
            throw new NullPointerException("base URL should be valid");
        }

        Merchant.merchantToken = merchantToken;
        Merchant.baseUrl = baseUrl;
        this.context = context;

    }

    public  Observable<TrustedDevice> initialize(String emailOrPhone){
        User.emailOrMobile = emailOrPhone;
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.startService(Merchant.merchantToken,new CheckUserRequest(emailOrPhone,
                            context.getPackageName(),
                            new Phone(context).getDeviceMeta()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resp->{
                                subscriber.onNext(new TrustedDevice(context,
                                        Helper.getStatusPhone(context,resp.getStatus(),resp.getCode()),
                                        resp.getData()));
                                subscriber.onComplete();
                            }, subscriber::onError
                    );

        });

    }


}
