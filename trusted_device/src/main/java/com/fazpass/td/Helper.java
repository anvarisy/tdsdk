package com.fazpass.td;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class Helper {
    JSONArray listContactToJson(@NonNull List<Contact> contacts){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i< contacts.size(); i++){
            JSONObject jsonObject = new JSONObject();
            String name = contacts.get(i).getName();
            try {
                jsonObject.put("name",name);
                jsonObject.put("phones",contacts.get(i).getPhoneNumber());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    JSONArray listSimToJson(@NonNull List<Sim> sims){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i< sims.size(); i++){
            JSONObject jsonObject = new JSONObject();
            String simSerial = sims.get(i).getSerialNumber();
            String phoneNumber = sims.get(i).getPhoneNumber();
            try {
                jsonObject.put("sim",simSerial);
                jsonObject.put("phone",phoneNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    static TD_STATUS getStatusPhone(Context context, boolean status, int code){
        if(!status){
            return TD_STATUS.USER_NOT_FOUND;
        }else if(code == 2000211){
            return TD_STATUS.KEY_SERVER_NOT_FOUND;
        }else if(code==2000212){
            return TD_STATUS.KEY_SERVER_NOT_FOUND;
        }else{
            String key = Storage.readDataLocal(context, TD.PRIVATE_KEY);
            if(key.equals("")){
                return TD_STATUS.KEY_LOCALE_NOT_FOUND;
            }
            return TD_STATUS.KEY_READY_TO_COMPARE;
        }

    }


}
