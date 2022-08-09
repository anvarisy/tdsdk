package com.fazpass.tdsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.fazpass.td.FazpassTd;
import com.fazpass.td.TrustedDevice;
import com.fazpass.td.User;
import com.fazpass.tdsample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.fazpass.tdsample.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        FazpassTd ftd = new FazpassTd(this,"qwertyuiop1123456","com.fazpass.tdsample","http://192.168.169.1:8080/");
        ftd.initialize("anvarisy@gmail.com").subscribe(trustedDevice -> {
            switch (trustedDevice.getStatus()){
                case TrustedDevice.KEY_LOCALE_NOT_FOUND:
                    //TODO
                    System.out.println("Key locale not found");
                    break;
                case TrustedDevice.KEY_IS_MATCH:
                    trustedDevice.enrollDeviceByPin(new User("","","","",""),"");
                    break;
            }
        },err->{

        });
    }
}