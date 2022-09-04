package com.fazpass.tdsample;

import static com.fazpass.tdsample.Cons.merchantKey;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fazpass.td.Fazpass;
import com.fazpass.td.RemoveStatus;
import com.fazpass.td.TD_MODE;
import com.fazpass.td.TD_STATUS;
import com.fazpass.td.TrustedDeviceListener;
import com.fazpass.td.User;
import com.fazpass.td.ValidateStatus;
import com.fazpass.tdsample.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Cons c = new Cons(this);
        String content = "Email: \t \t "+Storage.readDataLocal(this,"email")+"\n" +
                          "Phone: \t \t "+Storage.readDataLocal(this,"phone")+"\n" +
                          "Is Trusted: \t \t"+Boolean.parseBoolean(Storage.readDataLocal(this,"status"));
        binding.tvContent.setText(content);
        binding.btnLogout.setOnClickListener(v -> {
            c.showDialog();
            Fazpass.initialize(this, merchantKey, TD_MODE.STAGING).check(
                    Storage.readDataLocal(this,"email"),Storage.readDataLocal(this,"phone")
            ).subscribe(f->{
                if(f.status.equals(TD_STATUS.KEY_IS_MATCH)){
                    f.removeDevice(new TrustedDeviceListener<RemoveStatus>() {
                        @Override
                        public void onSuccess(RemoveStatus result) {
                            Storage.removeData(HomeActivity.this);
                            Intent intent  = new Intent(HomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(Throwable err) {
                            c.closeDialog();
                            Toast.makeText(HomeActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            },err->{
                c.closeDialog();
            });
        });

     binding.btnValidate.setOnClickListener(v->{
         c.showDialog();
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View viewInput = inflater.inflate(R.layout.dialog_pin,null,false);
         EditText pin = viewInput.findViewById(R.id.edtInputPin);
         Button confirm = viewInput.findViewById(R.id.btnSubmit);
         Fazpass.initialize(this, merchantKey, TD_MODE.STAGING).check(
                 Storage.readDataLocal(this,"email"),Storage.readDataLocal(this,"phone"))
                 .subscribe(f->{
                     c.closeDialog();
                     if(f.status.equals(TD_STATUS.KEY_IS_MATCH)){
                         if(!User.isUseFinger()){
                             AlertDialog.Builder builder = new AlertDialog.Builder(this);
                             builder.setView(viewInput)
                                     .setTitle("PIN Confirmation");
                             AlertDialog dialog = builder.create();
                             dialog.show();
                             confirm.setOnClickListener(views->{
                                 dialog.dismiss();
                                 c.showDialog();
                                 f.validateUser(pin.getText().toString(), new TrustedDeviceListener<ValidateStatus>() {
                                     @Override
                                     public void onSuccess(ValidateStatus result) {
                                         c.closeDialog();
                                         double total = (result.getConfidenceRate().getContact()+
                                                 result.getConfidenceRate().getSim()+
                                                 result.getConfidenceRate().getKey()+
                                                 result.getConfidenceRate().getMeta()+
                                                 result.getConfidenceRate().getLocation())*100;
                                         String data = "Contact:\t\t "+result.getConfidenceRate().getContact()+"\n" +
                                                 "Sim:\t\t "+result.getConfidenceRate().getSim()+"\n" +
                                                 "Key:\t\t "+result.getConfidenceRate().getKey()+"\n" +
                                                 "Meta:\t\t "+result.getConfidenceRate().getMeta()+"\n" +
                                                 "Location:\t\t "+result.getConfidenceRate().getLocation()+"\n"+
                                                 "Total:\t\t "+total+"\n";
                                         binding.tvDetail.setText(data);
                                     }

                                     @Override
                                     public void onFailure(Throwable err) {
                                         c.closeDialog();
                                         binding.tvDetail.setText(err.getMessage());
                                     }
                                 });
                             });


                         }else{
                             f.validateUser("", new TrustedDeviceListener<ValidateStatus>() {
                                 @Override
                                 public void onSuccess(ValidateStatus result) {
                                     c.closeDialog();
                                     double total = (result.getConfidenceRate().getContact()+
                                             result.getConfidenceRate().getSim()+
                                             result.getConfidenceRate().getKey()+
                                             result.getConfidenceRate().getMeta()+
                                             result.getConfidenceRate().getLocation())*100;
                                     String data = "Contact:\t\t "+result.getConfidenceRate().getContact()+"\n" +
                                             "Sim:\t\t "+result.getConfidenceRate().getSim()+"\n" +
                                             "Key:\t\t "+result.getConfidenceRate().getKey()+"\n" +
                                             "Meta:\t\t "+result.getConfidenceRate().getMeta()+"\n" +
                                             "Location:\t\t "+result.getConfidenceRate().getLocation()+"\n"+
                                             "Total:\t\t "+total+"\n";
                                     binding.tvDetail.setText(data);
                                 }

                                 @Override
                                 public void onFailure(Throwable err) {
                                     c.closeDialog();
                                     binding.tvDetail.setText(err.getMessage());
                                 }
                             });
                         }
                     }
                 },err->{
                     c.closeDialog();
                     Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
                 });
     });
    }
}