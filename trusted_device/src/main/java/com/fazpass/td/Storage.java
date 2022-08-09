package com.fazpass.td;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

public class Storage {
    private Context context;

    public Storage(Context context) {
        this.context = context;
    }

    static void storeToLocal(Context context, String key, String value){
        String password = Merchant.merchantToken;
        SharedPreferences sharedPref = context.getSharedPreferences(password,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    static String readDataLocal(Context context, String key){
        String password = Merchant.merchantToken;
        SharedPreferences sharedPref = context.getSharedPreferences(password,Context.MODE_PRIVATE);
        return sharedPref.getString(key,"");
    }

     static String readDataPublic(@NonNull Context context, String packageName, String password, String key){
        try{
            Context packageContext = context.createPackageContext(packageName,0);
            SharedPreferences pref = packageContext.getSharedPreferences(
                    password, Context.MODE_PRIVATE);
            return pref.getString(key,"");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }
}
