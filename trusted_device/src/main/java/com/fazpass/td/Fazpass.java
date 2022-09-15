package com.fazpass.td;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.fazpass.td.internet.Response;
import com.fazpass.td.internet.Roaming;
import com.fazpass.td.internet.UseCase;
import com.fazpass.td.internet.request.CheckUserRequest;
import com.fazpass.td.internet.request.EnrollDeviceRequest;
import com.fazpass.td.internet.request.RemoveDeviceRequest;
import com.fazpass.td.internet.request.ValidateDeviceRequest;
import com.fazpass.td.internet.response.CheckUserResponse;
import com.fazpass.td.internet.response.EnrollDeviceResponse;
import com.fazpass.td.internet.response.RemoveDeviceResponse;
import com.fazpass.td.internet.response.ValidateDeviceResponse;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import at.favre.lib.crypto.bcrypt.BCrypt;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.sentry.Sentry;
import io.sentry.android.core.SentryAndroid;

public class Fazpass extends TrustedDevice implements Behaviour {


    public static TrustedDevice initialize(Context context, String merchantToken, TD_MODE mode){
        Device device = new Device(context);
        SentryAndroid.init(context, options -> {
            options.setDsn("https://1f85de8be5544aaab7847e377b4c6227@o1173329.ingest.sentry.io/6720667");
            options.setTracesSampleRate(1.0);
        });
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
                break;
        }
        Merchant.merchantToken = merchantToken;
        return new Fazpass(context);
    }

    private Fazpass( Context context){
        ctx = context;
    }

    private Fazpass(Context context, TD_STATUS st, CheckUserResponse resp){
        if(!st.equals(TD_STATUS.KEY_READY_TO_COMPARE)){
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
            } catch (Exception e) {
                status = TD_STATUS.KEY_NOT_MATCH;
            }
        }
        ctx = context;
    }

    @Override
    public Observable<Fazpass> check(@NonNull String email, @NonNull String phone) {
        if(email.equals("") && phone.equals("")){
            throw new NullPointerException("email or phone cannot be empty");
        }
        String packageName = ctx.getPackageName();
        Device device = new Device(ctx);
        GeoLocation location = new GeoLocation(ctx);
        CheckUserRequest.Location locationDetail = new CheckUserRequest.Location(location.getLatitude(),location.getLongitude());
        CheckUserRequest body = new CheckUserRequest(email,phone, packageName,device.getDevice(), location.getTimezone(),locationDetail);
        Helper.sentryMessage("CHECK", body);
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.startService("Bearer "+Merchant.merchantToken,body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resp->{
                                String key = Storage.readDataLocal(ctx, PRIVATE_KEY);
                                if(!resp.getStatus()){
                                    subscriber.onNext(new Fazpass(ctx, TD_STATUS.USER_NOT_FOUND, null));
                                    subscriber.onComplete();
                                }else{
                                    if(resp.getData().getApps().getCurrent().getKey().equals("")){
                                        subscriber.onNext(new Fazpass(ctx, TD_STATUS.KEY_SERVER_NOT_FOUND, null));
                                        subscriber.onComplete();
                                    }else{
                                        if(key.equals("")){
                                            removeDevice(resp.getData().getUser().getId(),resp.getData()).subscribe(f->{
                                                subscriber.onNext(f);
                                                subscriber.onComplete();
                                            });
                                        }else{
                                            subscriber.onNext(new Fazpass(ctx, TD_STATUS.KEY_READY_TO_COMPARE, resp.getData()));
                                            subscriber.onComplete();
                                        }
                                    }

                                }

                            },err->{
                                subscriber.onError(err);
                                Sentry.captureException(err);
                            }
                    );
        });
    }

    @Override
    public void enrollDeviceByPin(User user, @NonNull String pin, TrustedDeviceListener<EnrollStatus> enroll) {
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        EnrollDeviceRequest body = collectDataEnroll(user,pin, false);
        Helper.sentryMessage("ENROLL_DEVICE_BY_PIN", body);
        enroll(body).subscribe(resp-> enroll.onSuccess(new EnrollStatus(resp.getStatus(),resp.getMessage())),
                err->{
                    enroll.onFailure(err);
                    Sentry.captureException(err);
                });
    }

    @Override
    public void enrollDeviceByFinger(User user, String pin, TrustedDeviceListener<EnrollStatus> enroll) {
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        openBiometric(new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Exception e = Error.biometricError();
                enroll.onFailure(e);
                Sentry.captureException(e);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                EnrollDeviceRequest body = collectDataEnroll(user, pin, true);
                Helper.sentryMessage("ENROLL_DEVICE_BY_FINGER", body);
                enroll(body).subscribe(resp-> enroll.onSuccess(new EnrollStatus(resp.getStatus(), resp.getMessage())), enroll::onFailure);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Exception e = Error.biometricFailed();
                enroll.onFailure(e);
                Sentry.captureException(e);
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

    @Override
    public void removeDevice(TrustedDeviceListener<RemoveStatus> listener) {
        String userId = Storage.readDataLocal(ctx,USER_ID);
        RemoveDeviceRequest body = collectDataRemove(userId);
        Helper.sentryMessage("REMOVE_DEVICE", body);
        remove(body).subscribe(resp->{
           listener.onSuccess(new RemoveStatus(resp.getStatus(),resp.getMessage()));
           Storage.removeDataLocal(ctx);
        }, err->{
            listener.onFailure(err);
            Sentry.captureException(err);
        });
    }

    //Usage to remove device from server cause key on local is missing
    private Observable<Fazpass> removeDevice(String userId, CheckUserResponse resp) {
      return Observable.create(subscriber->{
          UseCase u = Roaming.start();
          Helper.sentryMessage("FORCE_REMOVE_DEVICE",collectDataRemove(userId));
          u.removeDevice("Bearer "+Merchant.merchantToken,collectDataRemove(userId))
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(s->{
                      subscriber.onNext(new Fazpass(ctx,TD_STATUS.KEY_LOCALE_NOT_FOUND, resp));
                      subscriber.onComplete();
                  },err->{

                  });
      });
    }

    private void validateUserByFinger(TrustedDeviceListener<ValidateStatus> listener) {
        openBiometric(new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Exception e = Error.biometricError();
                listener.onFailure(e);
                Sentry.captureException(e);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                ValidateDeviceRequest body = collectDataValidate();
                assert body != null;
                if(body.getKey().equals("")){
                    listener.onFailure(Error.localDataMissing());
                    return;
                }
                Helper.sentryMessage("VALIDATE_BY_FINGER",body);
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
                Exception e = Error.biometricFailed();
                listener.onFailure(e);
                Sentry.captureException(e);
            }
        });
    }

    private void validateUserByPin(String pin, TrustedDeviceListener<ValidateStatus> listener) {
        if(pin.equals("")){
            throw new NullPointerException("PIN cannot be null or empty");
        }
        String meta = Storage.readDataLocal(ctx,Fazpass.META);
        String key = Storage.readDataLocal(ctx,Fazpass.PRIVATE_KEY);
        if(meta.equals("")||key.equals("")){
            listener.onFailure(Error.localDataMissing());
            return;
        }
        String rawData = Crypto.decrypt(meta,key);
        try{
            JSONObject json = new JSONObject(rawData);
            String cryptPin = json.getString(Fazpass.USER_PIN);
            BCrypt.Result result = BCrypt.verifyer().verify(pin.toCharArray(), cryptPin);
            if(result.verified){
                ValidateDeviceRequest body = collectDataValidate();
                assert body != null;
                if(body.getKey().equals("")){
                    listener.onFailure(Error.localDataMissing());
                    return;
                }
                Helper.sentryMessage("VALIDATE_BY_PIN",body);
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
            Sentry.captureException(e);
            e.printStackTrace();
        }
    }

    private void openBiometric(BiometricPrompt.AuthenticationCallback listener){
        Executor executor = ContextCompat.getMainExecutor(ctx);
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) ctx, executor,listener);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Required")
                .setSubtitle("")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    private Observable<Response<EnrollDeviceResponse>> enroll(EnrollDeviceRequest body){
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.enrollDevice("Bearer "+Merchant.merchantToken,body)
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
            u.validateDevice("Bearer "+Merchant.merchantToken,body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp->{
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    private Observable<Response<RemoveDeviceResponse>> remove(RemoveDeviceRequest body){
        return Observable.create(subscriber->{
            UseCase u = Roaming.start();
            u.removeDevice("Bearer "+Merchant.merchantToken,body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp->{
                        subscriber.onNext(resp);
                        subscriber.onComplete();
                    }, subscriber::onError);
        });
    }

    @NonNull
    private EnrollDeviceRequest collectDataEnroll(User user, String pin, boolean isUseFinger){
        Sim sim = new Sim(ctx);
        Device device = new Device(ctx);
        Connection c = new Connection(ctx);
        FazpassKey key = new FazpassKey(ctx,user,device,pin);
        GeoLocation geo = new GeoLocation(ctx);
        Contact contact = new Contact(ctx);
        List<EnrollDeviceRequest.Contact> contactBody = new ArrayList<>();
        if(contact.getContacts().size()>0){
            for(Contact ct: contact.getContacts()){
                if(ct.getPhoneNumber().size()>0){
                    EnrollDeviceRequest.Contact ctBody = new EnrollDeviceRequest.Contact(ct.getName(),ct.getPhoneNumber().get(0));
                    contactBody.add(ctBody);
                }else {
                    EnrollDeviceRequest.Contact ctBody = new EnrollDeviceRequest.Contact(ct.getName(),"");
                    contactBody.add(ctBody);
                }
            }
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

    private ValidateDeviceRequest collectDataValidate(){
        String userId = Storage.readDataLocal(ctx,Fazpass.USER_ID);
        String meta = Storage.readDataLocal(ctx,Fazpass.META);
        String key = Storage.readDataLocal(ctx,Fazpass.PUBLIC_KEY);
        if(userId.equals("")||meta.equals("")||key.equals("")){
            return null;
        }
        Sim sim = new Sim(ctx);
        Device device = new Device(ctx);
        GeoLocation geo = new GeoLocation(ctx);
        Contact contact = new Contact(ctx);

        List<ValidateDeviceRequest.Contact> contactBody = new ArrayList<>();
        if(contact.getContacts().size()>0){
            for(Contact ct: contact.getContacts()){
                if(ct.getPhoneNumber().size()>0){
                    ValidateDeviceRequest.Contact ctBody = new ValidateDeviceRequest.Contact(ct.getName(),ct.getPhoneNumber().get(0));
                    contactBody.add(ctBody);
                }else {
                    ValidateDeviceRequest.Contact ctBody = new ValidateDeviceRequest.Contact(ct.getName(),"");
                    contactBody.add(ctBody);
                }
            }
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

    private RemoveDeviceRequest collectDataRemove(String userId){
        Device device = new Device(ctx);
        GeoLocation geo = new GeoLocation(ctx);
        RemoveDeviceRequest.Location l = new RemoveDeviceRequest.Location(geo.getLatitude(), geo.getLongitude());
        RemoveDeviceRequest body = new RemoveDeviceRequest(userId,ctx.getPackageName(),device.getDevice(),l,geo.getTimezone());
        return body;
    }
}
