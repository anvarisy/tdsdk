package com.fazpass.td;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

class Storage {

    private static void saveData(Context context, String key, String value){
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

    private static void removeDataLocal(Context context, String key){
        String password = Merchant.merchantToken;
        SharedPreferences sharedPref = context.getSharedPreferences(password,Context.MODE_PRIVATE);
        sharedPref.edit().remove(key).apply();

    }

    static void storeDataLocal(Context context, String key, String newValue){
        if(isDataExists(context,key)){
            removeDataLocal(context,key);
        }
        saveData(context,key,newValue);
    }

    private static boolean isDataExists(Context context, String key){
        return !readDataLocal(context, key).equals("");
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
