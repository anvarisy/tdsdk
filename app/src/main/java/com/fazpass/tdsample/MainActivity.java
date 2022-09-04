package com.fazpass.tdsample;

import static com.fazpass.tdsample.Cons.merchantKey;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fazpass.td.EnrollStatus;
import com.fazpass.td.Fazpass;
import com.fazpass.td.TD_MODE;
import com.fazpass.td.TrustedDeviceListener;
import com.fazpass.td.User;
import com.fazpass.tdsample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        String email = Storage.readDataLocal(this, "email");
        String phone = Storage.readDataLocal(this, "phone");
        Cons c = new Cons(this);
        binding.btnLogin.setOnClickListener(v->{
            if(email.equals("")&&phone.equals("")){
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return;
            }
            c.showDialog();
             Fazpass.initialize(this, merchantKey, TD_MODE.STAGING)
                .check(email,phone).subscribe(f->{
                    switch (f.status){
                        case KEY_IS_MATCH:
                            Intent intent = new Intent(this, HomeActivity.class);
                            startActivity(intent);
                            break;
                        default:
                            Intent loginIntent = new Intent(this, LoginActivity.class);
                            startActivity(loginIntent);
                            break;
                    }
                },err->{
                         Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
        binding.btnRegister.setOnClickListener(v->{
            Intent in = new Intent(this, RegisterActivity.class);
            startActivity(in);
        });
    }
    private void showDialog(String message){

    }
}