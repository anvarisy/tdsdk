package com.fazpass.td;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fazpass.td.internet.Response;
import com.fazpass.td.internet.Roaming;
import com.fazpass.td.internet.UseCase;
import com.fazpass.td.internet.request.EnrollDeviceRequest;
import com.fazpass.td.internet.response.CheckUserResponse;
import com.fazpass.td.internet.response.EnrollDeviceResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TrustedDevice {
    private Context context;
    private TD status;

    public TD getStatus() {
        return status;
    }

    public TrustedDevice(Context context) {
        this.context = context;
    }

    TrustedDevice(Context context, TD st, CheckUserResponse resp) {
        if(!Objects.equals(st, TD.KEY_READY_TO_COMPARE)){
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
                    this.status = TD.KEY_IS_MATCH;
                    //TODO update last active
                }else{
                    this.status = TD.KEY_NOT_MATCH;
                }

            } catch (JSONException e) {
                Log.e("TD",e.getMessage());
                e.printStackTrace();
                this.status = TD.KEY_NOT_MATCH;
            }
        }
        this.context = context;
    }

    public void enrollDeviceByPin(User user, @NonNull String pin, EnrollDevice<EnrollStatus> enroll){
        Sim sim = new Sim(context);
        Device device = new Device(context);
        Connection c = new Connection(context);
        Key key = new Key(context,user,device,pin);
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
                device.getDevice(), context.getPackageName(),true,false,true, c.isUseVpn(),
                device.getNotificationToken(),key.getMeta(),key.getUserId(),geo.getTimezone(),contactBody,locationBody,simBody);
        enroll(request).subscribe(resp->{

        },err->{

        });
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


}
