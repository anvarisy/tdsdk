package com.fazpass.td;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

class Storage {
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

    static void removeDataLocal(Context context, String key){
        String password = Merchant.merchantToken;
        SharedPreferences sharedPref = context.getSharedPreferences(password,Context.MODE_PRIVATE);
        sharedPref.edit().remove(key);
    }

    static boolean isDataExists(Context context, String key){
        if(readDataLocal(context,key).equals("")){
            return false;
        }
        return true;
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
