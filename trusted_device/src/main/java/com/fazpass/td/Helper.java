package com.fazpass.td;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fazpass.td.internet.Response;
import com.fazpass.td.internet.response.CheckUserResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class Helper {

    static TD_STATUS getStatusPhone(Response<CheckUserResponse> resp){
        if(!resp.getStatus()){
            return TD_STATUS.USER_NOT_FOUND;
        }
        if(resp.getData().getApps().getCurrent().getKey().equals("")){
            return TD_STATUS.KEY_SERVER_NOT_FOUND;
        }else{
            return TD_STATUS.KEY_READY_TO_COMPARE;
        }
    }


}
