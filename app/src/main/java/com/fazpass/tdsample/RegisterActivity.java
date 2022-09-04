package com.fazpass.tdsample;

import static com.fazpass.tdsample.Cons.merchantKey;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fazpass.td.EnrollStatus;
import com.fazpass.td.Fazpass;
import com.fazpass.td.TD_MODE;
import com.fazpass.td.TD_STATUS;
import com.fazpass.td.TrustedDeviceListener;
import com.fazpass.td.User;
import com.fazpass.tdsample.databinding.ActivityMainBinding;
import com.fazpass.tdsample.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private String email = "";
    private String phone = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnRegister.setOnClickListener(v->{

            if(binding.edtUserId.getText().toString().equals("")
               ||binding.edtUserPin.getText().toString().equals("")
               ||binding.edtUserConfirmPin.getText().toString().equals("")
               ||binding.edtUserEmail.getText().toString().equals("")){
                Toast.makeText(this, "Please fill all field", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!binding.edtUserPin.getText().toString().equals(binding.edtUserConfirmPin.getText().toString())){
                Toast.makeText(this, "PIN not match", Toast.LENGTH_SHORT).show();
                return;
            }
            phone = binding.edtUserId.getText().toString();
            email = binding.edtUserEmail.getText().toString();
            Fazpass.initialize(this, merchantKey, TD_MODE.STAGING).check(email,phone)
                    .subscribe(f->{
                      if(f.status.equals(TD_STATUS.USER_NOT_FOUND)||f.status.equals(TD_STATUS.KEY_SERVER_NOT_FOUND)){
                          AlertDialog.Builder builder = new AlertDialog.Builder(this);
                          builder.setNeutralButton("NO", (dialogInterface, i) -> {
                              goHome(false);
                          });
                          builder.setPositiveButton("YES BY FINGER", (dialogInterface, i) -> {
                              f.enrollDeviceByFinger(new User(email, phone, "", "", ""),
                                      binding.edtUserPin.getText().toString(), new TrustedDeviceListener<EnrollStatus>() {
                                          @Override
                                          public void onSuccess(EnrollStatus result) {
                                                goHome(true);
                                          }

                                          @Override
                                          public void onFailure(Throwable err) {
                                              Toast.makeText(RegisterActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
                                          }
                                      });
                          });

                          builder.setNegativeButton("YES BY PIN", (dialogInterface, i) -> {

                              f.enrollDeviceByPin(new User(email, phone, "", "", ""),
                                      binding.edtUserPin.getText().toString(), new TrustedDeviceListener<EnrollStatus>() {
                                          @Override
                                          public void onSuccess(EnrollStatus result) {
                                              goHome(true);
                                          }

                                          @Override
                                          public void onFailure(Throwable err) {
                                              Toast.makeText(RegisterActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
                                          }
                                      });
                          });
                          builder.setCancelable(false);
                          builder.setMessage("Do you want activating trusted device ?")
                                  .setTitle("TRUSTED DEVICE");
                          AlertDialog dialog = builder.create();
                          dialog.show();
                      }
                    },err->{

            });
        });
    }

    private void goHome(boolean status){
        Storage.storeDataLocal(this,"email",email);
        Storage.storeDataLocal(this,"phone",phone);
        Storage.storeDataLocal(this,"status",Boolean.toString(status));
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}