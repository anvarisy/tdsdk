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
import com.fazpass.tdsample.databinding.ActivityLoginBinding;
import com.fazpass.tdsample.databinding.ActivityRegisterBinding;

public class LoginActivity extends AppCompatActivity {
    private String email = "";
    private String phone = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Cons c = new Cons(this);
        c.closeDialog();
        binding.btnLoginPage.setOnClickListener(v -> {
            if(binding.edtUserId.getText().toString().equals("")){
                Toast.makeText(this, "Please fill user id field !", Toast.LENGTH_SHORT).show();
                return;
            }
            c.showDialog(false);
            if(isEmailValid(binding.edtUserId.getText().toString())){
                email = binding.edtUserId.getText().toString();
            }else{
                phone = binding.edtUserId.getText().toString();
            }
            Fazpass.initialize(this, merchantKey, TD_MODE.STAGING).check(email,phone).subscribe(f->{
                c.closeDialog();
                switch (f.status){
                    case KEY_IS_MATCH:
                        goHome(true);
                        break;
                    case USER_NOT_FOUND:
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setPositiveButton("YES", (dialogInterface, i) -> {
                            Intent intent = new Intent(this, RegisterActivity.class);
                            startActivity(intent);

                        });
                        builder.setNegativeButton("NO", (dialogInterface, i) -> {
                           dialogInterface.dismiss();
                        });
                        builder.setCancelable(false);
                        builder.setMessage("Do you want to register ?")
                                .setTitle("Account Not Found");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;
                    default:
                        builder = new AlertDialog.Builder(this);
                        builder.setPositiveButton("YES", (dialogInterface, i) -> {
                            Intent intent = new Intent(this, RegisterActivity.class);
                            startActivity(intent);
                        });
                        builder.setNegativeButton("NO", (dialogInterface, i) -> {
                            goHome(false);
                        });
                        builder.setCancelable(false);
                        builder.setMessage("Do you want activating trusted device ?")
                                .setTitle("TRUSTED DEVICE");
                        dialog = builder.create();
                        dialog.show();
                        break;
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