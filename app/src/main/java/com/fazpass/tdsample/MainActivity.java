package com.fazpass.tdsample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.fazpass.td.EnrollStatus;
import com.fazpass.td.Fazpass;
import com.fazpass.td.TD_MODE;
import com.fazpass.td.TrustedDeviceListener;
import com.fazpass.td.User;
import com.fazpass.tdsample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.fazpass.tdsample.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Fazpass.initialize(this,"qwertyuiop1123456", TD_MODE.DEBUG)
                .check("","").subscribe(f->{
                    f.enrollDeviceByFinger(new User("","","","",""), "", new TrustedDeviceListener<EnrollStatus>() {
                        @Override
                        public void onSuccess(EnrollStatus result) {

                        }

                        @Override
                        public void onFailure(Throwable err) {

                        }
                    });
                },err->{

                });

    }

}