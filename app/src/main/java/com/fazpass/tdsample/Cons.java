package com.fazpass.tdsample;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.basusingh.beautifulprogressdialog.BeautifulProgressDialog;

public class Cons {
    public static final String merchantKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjoxM30.SbTzA7ftEfUtkx0Rdt_eoXrafx1X9kf2SHccS_G5jS8";
    private Context context;
    private  BeautifulProgressDialog progressDialog;
    public Cons(Context context) {
        this.context = context;
        progressDialog = new BeautifulProgressDialog((AppCompatActivity)context,
                BeautifulProgressDialog.withImage,
                "Please wait");
        progressDialog.setImageLocation(ResourcesCompat.getDrawable(context.getResources(), R.drawable.fazpass, null));
        progressDialog.setLayoutColor(ResourcesCompat.getColor(context.getResources(),R.color.blue,null));

    }

    public void showDialog(boolean cancelAble){
        progressDialog.setCancelable(cancelAble);
        progressDialog.show();
    }

    public void closeDialog(){
        progressDialog.dismiss();
    }
}
