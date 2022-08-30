package com.fazpass.td;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.fazpass.td.internet.Response;
import com.fazpass.td.internet.Roaming;
import com.fazpass.td.internet.UseCase;
import com.fazpass.td.internet.request.CheckUserRequest;
import com.fazpass.td.internet.request.EnrollDeviceRequest;
import com.fazpass.td.internet.request.ValidateDeviceRequest;
import com.fazpass.td.internet.response.CheckUserResponse;
import com.fazpass.td.internet.response.EnrollDeviceResponse;
import com.fazpass.td.internet.response.ValidateDeviceResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Fazpass extends TrustedDevice implements Behaviour {


    public static TrustedDevice initialize(Context context, String merchantToken, TD_MODE mode){
        Device device = new Device(context);
        if (merchantToken == null || merchantToken.equals("")){
            throw new NullPointerException("merchant id cannot be null or empty");
        }else if (device.isRooted() || device.isEmulator()){
            throw new SecurityException("Device rooted or is an emulator");
        }
        switch (mode){
            case DEBUG:
                Merchant.baseUrl = Fazpass.DEBUG;
                break;
            case STAGING:
                Merchant.baseUrl = Fazpass.STAGING;
                break;
            case PRODUCTION:
                Merchant.baseUrl = Fazpass.PRODUCTION;
        }
        Merchant.merchantToken = merchantToken;
        return new Fazpass(context);
    }

    private Fazpass( Context context){
        ctx = context;
    }

    private Fazpass(Context context, TD_STATUS st, CheckUserResponse resp){
        if(!Objects.equals(st, TD_STATUS.KEY_READY_TO_COMPARE)){
            Log.e("TD","FAILED TO COMPARE");
            status = st;
        }else{
            Device device = new Device(context);
            String password = Storage.readDataLocal(context,Fazpass.PRIVATE_KEY);
            try{
                String hashedInformation = resp.getApps().getCurrent().getMeta();
                String jsonString = Crypto.decrypt(hashedInformation,password);
                JSONObject json = new JSONObject(jsonString);
                if(json.getString(Fazpass.MERCHANT_TOKEN).equals(Merchant.merchantToken)&&
                        json.getString(Fazpass.PACKAGE_NAME).equals(context.getPackageName())&&
                        json.getString(Fazpass.DEVICE).equals(device.getDevice())){
                    status = TD_STATUS.KEY_IS_MATCH;
                    Storage.storeDataLocal(context,Fazpass.USER_ID, resp.getUser().getId());
                    User.setIsUseFinger(resp.getApps().getCurrent().isUse_fingerprint());
                    //TODO update last active
                }else{
                    status = TD_STATUS.KEY_NOT_MATCH;
                }

            } catch (JSONException e) {
                Log.e(TAG,e.getMessage());
                status = TD_STATUS.KEY_NOT_MATCH;
            }
        }
        ctx = context;
    }

    @Override
    public Observable<Fazpass> check(String email, String phone) {
        String packageName = ctx.getPackageName();
        Device device = new Device(ctx);
        GeoLocation location = new GeoLocation(ctx);
        CheckUserRequest.Location locationDetail = new CheckUserRequest.Location(location.getLatitude(),location.getLongitude());
        CheckUserRequest body = new CheckUserRequest(email,phone, packageName,device.getDevice(), location.getTimezone(),locationDetail);
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.startService(Merchant.merchantToken,body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resp->{
                                subscriber.onNext(new Fazpass(ctx,
                                        Helper.getStatusPhone(ctx,resp.getStatus(),resp.getCode()),
                                        resp.getData()));
                                subscriber.onComplete();
                            },err->{
                                subscriber.onError(err);
                                Log.e(Fazpass.TAG,err.getMessage());
                            }
                    );
        });
    }

    @Override
    public void enrollDeviceByPin(User user, @NonNull String pin, TrustedDeviceListener<EnrollStatus> enroll) {
        EnrollDeviceRequest request = collectDataEnroll(user,pin, false);
        enroll(request).subscribe(resp-> enroll.onSuccess(new EnrollStatus(resp.getStatus(),resp.getMessage())), err->{
            Log.e(Fazpass.TAG,err.getMessage());
            enroll.onFailure(err);
        });
    }

    @Override
    public void enrollDeviceByFinger(User user, String pin, TrustedDeviceListener<EnrollStatus> enroll) {
        openBiometric(new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                enroll.onFailure(new Exception("Biometric error"));
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                EnrollDeviceRequest request = collectDataEnroll(user, pin, true);
                enroll(request).subscribe(resp-> enroll.onSuccess(new EnrollStatus(resp.getStatus(), resp.getMessage())), err->{
                    Log.e(Fazpass.TAG,err.getMessage());
                    enroll.onFailure(err);
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                enroll.onFailure(new Exception("Biometric failed"));
            }
        });
    }

    @Override
    public void validateUser(String pin, TrustedDeviceListener<ValidateStatus> listener) {
        if(User.isUseFinger()){
            validateUserByFinger(listener);
        }else{
            validateUserByPin(pin, listener);
        }
    }

    private void validateUserByFinger(TrustedDeviceListener<ValidateStatus> listener) {
        openBiometric(new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                listener.onFailure(new Exception("Biometric error"));
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                ValidateDeviceRequest body = collectDataValidate(listener);
                validate(body).subscribe(resp->{
                    ValidateStatus.Confidence cfd = new ValidateStatus.Confidence(
                            resp.getData().getMeta(),
                            resp.getData().getKey(),
                            resp.getData().getSim(),
                            resp.getData().getContact(),
                            resp.getData().getContact()
                    );
                    ValidateStatus status = new ValidateStatus(resp.getStatus(),cfd);
                    listener.onSuccess(status);
                },err->{
                    Log.e(Fazpass.TAG,err.getMessage());
                    listener.onFailure(err);
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                listener.onFailure(new Exception("Biometric failed"));
            }
        });
    }

    private void validateUserByPin(String pin, TrustedDeviceListener<ValidateStatus> listener) {
        String meta = Storage.readDataLocal(ctx,Fazpass.META);
        String key = Storage.readDataLocal(ctx,Fazpass.PRIVATE_KEY);
        if(meta.equals("")||key.equals("")){
            listener.onFailure(Error.localDataMissing());
        }
        String rawData = Crypto.decrypt(meta,key);
        try{
            JSONObject json = new JSONObject(rawData);
            String cryptPin = json.getString(Fazpass.USER_PIN);
            BCrypt.Result result = BCrypt.verifyer().verify(pin.toCharArray(), cryptPin);
            if(result.verified){
                ValidateDeviceRequest body = collectDataValidate(listener);
                validate(body).subscribe(resp->{
                    ValidateStatus.Confidence cfd = new ValidateStatus.Confidence(
                            resp.getData().getMeta(),
                            resp.getData().getKey(),
                            resp.getData().getSim(),
                            resp.getData().getContact(),
                            resp.getData().getContact()
                    );
                    ValidateStatus status = new ValidateStatus(resp.getStatus(),cfd);
                    listener.onSuccess(status);
                },err->{
                    Log.e(Fazpass.TAG,err.getMessage());
                    listener.onFailure(err);
                });
            }else{
                listener.onFailure(Error.pinNotMatch());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openBiometric(BiometricPrompt.AuthenticationCallback listener){
        Executor executor = ContextCompat.getMainExecutor(ctx);
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) ctx, executor,listener);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric required")
                .setSubtitle("")
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    private Observable<Response<EnrollDeviceResponse>> enroll(EnrollDeviceRequest body){
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.enrollDevice(Merchant.merchantToken,body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp->{
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    private Observable<Response<ValidateDeviceResponse>> validate(ValidateDeviceRequest body){
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.validateDevice(Merchant.merchantToken,body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp->{
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    @NonNull
    private EnrollDeviceRequest collectDataEnroll( User user, String pin, boolean isUseFinger){
        Sim sim = new Sim(ctx);
        Device device = new Device(ctx);
        Connection c = new Connection(ctx);
        FazpassKey key = new FazpassKey(ctx,user,device,pin);
        GeoLocation geo = new GeoLocation(ctx);
        Contact contact = new Contact(ctx);
        List<EnrollDeviceRequest.Contact> contactBody = new ArrayList<>();
        for(Contact ct: contact.getContacts()){
            EnrollDeviceRequest.Contact ctBody = new EnrollDeviceRequest.Contact(ct.getName(),ct.getPhoneNumber());
            contactBody.add(ctBody);
        }
        EnrollDeviceRequest.Location locationBody = new EnrollDeviceRequest.Location(geo.getLatitude(),geo.getLongitude());
        List<EnrollDeviceRequest.Sim> simBody = new ArrayList<>();
        for(Sim s: sim.getSims()){
            EnrollDeviceRequest.Sim smBody = new EnrollDeviceRequest.Sim(s.getSerialNumber(),s.getPhoneNumber());
            simBody.add(smBody);
        }
        EnrollDeviceRequest request = new EnrollDeviceRequest(
                user.getName(),user.getEmail(), user.getPhone(), user.getIdCard(), user.getAddress(),
                device.getDevice(), ctx.getPackageName(),true,isUseFinger,true, c.isUseVpn(),
                device.getNotificationToken(),key.getMeta(),key.getUserId(),geo.getTimezone(),contactBody,locationBody,simBody);
        return request;
    }

    private ValidateDeviceRequest collectDataValidate(TrustedDeviceListener<ValidateStatus> listener){
        String userId = Storage.readDataLocal(ctx,Fazpass.USER_ID);
        String meta = Storage.readDataLocal(ctx,Fazpass.META);
        String key = Storage.readDataLocal(ctx,Fazpass.PUBLIC_KEY);
        if(userId.equals("")||meta.equals("")||key.equals("")){
            listener.onFailure(Error.localDataMissing());
        }
        Sim sim = new Sim(ctx);
        Device device = new Device(ctx);
        GeoLocation geo = new GeoLocation(ctx);
        Contact contact = new Contact(ctx);

        List<ValidateDeviceRequest.Contact> contactBody = new ArrayList<>();
        for(Contact ct: contact.getContacts()){
            ValidateDeviceRequest.Contact ctBody = new ValidateDeviceRequest.Contact(ct.getName(),ct.getPhoneNumber());
            contactBody.add(ctBody);
        }
        ValidateDeviceRequest.Location locationBody = new ValidateDeviceRequest.Location(geo.getLatitude(),geo.getLongitude());
        List<ValidateDeviceRequest.Sim> simBody = new ArrayList<>();
        for(Sim s: sim.getSims()){
            ValidateDeviceRequest.Sim smBody = new ValidateDeviceRequest.Sim(s.getSerialNumber(),s.getPhoneNumber());
            simBody.add(smBody);
        }
        ValidateDeviceRequest body = new ValidateDeviceRequest(userId, device.getDevice(), ctx.getPackageName(),
                meta, key, geo.getTimezone(), contactBody, locationBody, simBody);
        return body;
    }
}
