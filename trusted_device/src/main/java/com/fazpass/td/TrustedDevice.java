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
import com.fazpass.td.internet.response.CheckUserResponse;
import com.fazpass.td.internet.response.EnrollDeviceResponse;

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

public class TrustedDevice {
    private Context context;
    private TD_STATUS status;




    public TrustedDevice(Context context, String merchantToken, String packageName, TD_MODE mode){
        Device device = new Device(context);
        if (!context.getPackageName().equals(packageName)){
            Log.e(Fazpass.TAG, String.format("your current package is %s that not match with %s",
                    context.getPackageName(),packageName));
            throw new SecurityException("package name not match");
        }
        else if (merchantToken == null || merchantToken.equals("")){
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

    TrustedDevice(Context context, TD_STATUS st, CheckUserResponse resp) {
        if(!Objects.equals(st, TD_STATUS.KEY_READY_TO_COMPARE)){
            Log.e("TD","FAILED TO COMPARE");
           this.status = st;
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
                    this.status = TD_STATUS.KEY_IS_MATCH;
                    if(Storage.isDataExists(context,Fazpass.USER_ID)){
                        Storage.removeDataLocal(context,Fazpass.USER_ID);
                    }
                    Storage.storeToLocal(context,Fazpass.USER_ID, resp.getUser().getId());
                    //TODO update last active
                }else{
                    this.status = TD_STATUS.KEY_NOT_MATCH;
                }

            } catch (JSONException e) {
                Log.e("TD",e.getMessage());
                e.printStackTrace();
                this.status = TD_STATUS.KEY_NOT_MATCH;
            }
        }
        this.context = context;
    }

    public final void enrollDeviceByPin(User user, @NonNull String pin, TrustedDeviceListener<EnrollStatus> enroll){
        EnrollDeviceRequest request = collectData(user,pin, false);
        enroll(request).subscribe(resp-> enroll.onSuccess(new EnrollStatus(resp.getStatus())), err->{
            Log.e(Fazpass.TAG,err.getMessage());
            enroll.onFailure(err);
        });
    }

    public final void enrollDeviceByFinger(User user, String pin, TrustedDeviceListener<EnrollStatus> enroll){
        openBiometric(new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                enroll.onFailure(new Exception("Biometric error"));
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                EnrollDeviceRequest request = collectData(user, pin, true);
                enroll(request).subscribe(resp-> enroll.onSuccess(new EnrollStatus(resp.getStatus())), err->{
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


    public final void validateUser(User user, String pin, boolean isUseFinger, TrustedDeviceListener<ValidateStatus> validate){
        if(isUseFinger){
            openBiometric(new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    validate.onFailure(new Exception("Biometric error"));
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    validate.onFailure(new Exception("Biometric failed"));
                }
            });
        }else{

        }
    }

    private void openBiometric(BiometricPrompt.AuthenticationCallback listener){
        Executor executor = ContextCompat.getMainExecutor(context);
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor,listener);
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

    @NonNull
    private EnrollDeviceRequest collectData(User user, String pin, boolean isUseFinger){
        Sim sim = new Sim(context);
        Device device = new Device(context);
        Connection c = new Connection(context);
        FazpassKey key = new FazpassKey(context,user,device,pin);
        GeoLocation geo = new GeoLocation(context);
        Contact contact = new Contact(context);
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
                device.getDevice(), context.getPackageName(),true,isUseFinger,true, c.isUseVpn(),
                device.getNotificationToken(),key.getMeta(),key.getUserId(),geo.getTimezone(),contactBody,locationBody,simBody);
        return request;
    }

}
