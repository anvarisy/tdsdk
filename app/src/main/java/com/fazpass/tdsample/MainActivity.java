package com.fazpass.tdsample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.fazpass.td.TD_MODE;
import com.fazpass.td.TrustedDevice;
import com.fazpass.tdsample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.fazpass.tdsample.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        TrustedDevice ftd = new TrustedDevice(this,"qwertyuiop1123456","com.fazpass.tdsample", TD_MODE.DEBUG);
    }

}