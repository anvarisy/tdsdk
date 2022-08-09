package com.fazpass.td;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;


import androidx.core.content.ContextCompat;

import com.fazpass.td.simhelper.SimDataPlugin;
import com.scottyab.rootbeer.RootBeer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Phone {
    private final String deviceMeta;
    private List<Contact> contacts;
    private final Boolean isRooted;
    private final List<Sim> sims;
    private final Context context;

    public String getDeviceMeta() {
        return deviceMeta;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public Boolean getRooted() {
        return isRooted;
    }

    public List<Sim> getSims() {
        return sims;
    }


    public Phone(Context context){
        this.context = context;
        try{
            this.contacts = readContacts().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        this.sims = readSims();
        this.isRooted = readRoot();
        this.deviceMeta = readMeta();

    }

    private String readMeta(){
        String meta = ""+ Build.BRAND+","+Build.MODEL+","+Build.VERSION.SDK_INT;
        Storage.storeToLocal(context,Fazpass.DEVICE_META,meta);
        return meta;
    }

    private Boolean readRoot(){
        RootBeer root = new RootBeer(context);
        Storage.storeToLocal(context, Fazpass.DEVICE_ROOTED, Boolean.valueOf(root.isRooted()).toString());
        return root.isRooted();
    }

    @SuppressLint("Range")
    private CompletableFuture<List<Contact>> readContacts(){
        CompletableFuture<List<Contact>> contactFuture = CompletableFuture.supplyAsync(()->{
            List<Contact> contacts = new ArrayList<>();
            JSONArray arr = new JSONArray();
            if(PackageManager.PERMISSION_GRANTED == ContextCompat
                    .checkSelfPermission(context, Manifest.permission.READ_CONTACTS)){
                ContentResolver cr =context.getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                while (cur.moveToNext()) {
                    List<String> phones = new ArrayList<>();
                    JSONObject jsonObject = new JSONObject();
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    try {
                        jsonObject.put("name",name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phones.add(phoneNo);

                        }
                        pCur.close();
                        try {
                            jsonObject.put("phones",phones);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    contacts.add(new Contact(name,phones));
                    arr.put(jsonObject);
                }
                cur.close();
                Storage.storeToLocal(context,Fazpass.DEVICE_CONTACTS,arr.toString());
            }else{
                contacts.add(new Contact("",null));
            }
            return contacts;

        });
        return contactFuture;

    }

    private List<Sim> readSims(){
        List<Sim> sims = new ArrayList<>();
        SimDataPlugin sdp = new SimDataPlugin(context);
        JSONObject json = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            try{
                json = sdp.getSimData().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            try{
                JSONArray arr = json.getJSONArray("cards");
                sims = validSims(arr);
            } catch (JSONException e) {
                sims.add(errSim());
                e.printStackTrace();
            }
            return sims;
        }else{
            try{
                json = sdp.getSimData().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            try{
                JSONArray arr = json.getJSONArray("cards");
                sims = validSims(arr);
            } catch (JSONException e) {
               sims.add(errSim());
               e.printStackTrace();
            }
            return sims;
        }
    }

    private Sim errSim(){
        return new Sim("","");
    }

    private List<Sim> validSims(JSONArray jsonArray){
        Storage.storeToLocal(context,Fazpass.DEVICE_SIMS,jsonArray.toString());
        List<Sim> sims = new ArrayList<>();
        for(int i = 0; i< jsonArray.length(); i++){
            try{
                JSONObject obj = jsonArray.getJSONObject(i);
                String phoneNumber = obj.getString(Fazpass.Sim.PHONE_NUMBER);
                String carrierName = obj.getString(Fazpass.Sim.CARRIER_NAME);
                String serialNumber = obj.getString(Fazpass.Sim.SERIAL_NUMBER);
                Sim sim = new Sim(serialNumber,phoneNumber);
                sims.add(sim);
                Log.e("Sim Carrier",carrierName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return sims;
    }
}
