package com.fazpass.tdsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.fazpass.td.EnrollDevice;
import com.fazpass.td.EnrollStatus;
import com.fazpass.td.FazpassTd;
import com.fazpass.td.TD;
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
        FazpassTd ftd = new FazpassTd(this,"qwertyuiop1123456","com.fazpass.tdsample","http://192.168.54.219:8080/");
        ftd.initialize("anvarisy@gmail.com","085811751000").subscribe(td->{
            if(!td.getStatus().equals(TD.KEY_IS_MATCH)){
                Log.e("STATUS",td.getStatus().toString());
//                enroll(td);
            }else{
                Log.e("STATUS","READY TO LAST ACTIVE");
            }
        },err->{
            Log.e("ERROR",err.getMessage());
        });
    }

    private void enroll(TrustedDevice trustedDevice){
        User nUser = new User("anvarisy@gmail.com","","","","");
        trustedDevice.enrollDeviceByPin(nUser, "", new EnrollDevice<EnrollStatus>() {
            @Override
            public void enrollDeviceSuccess(EnrollStatus result) {

            }

            @Override
            public void enrollDeviceFailure(Throwable err) {

            }
        });
    }
}