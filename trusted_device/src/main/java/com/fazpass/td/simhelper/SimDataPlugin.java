package com.fazpass.td.simhelper;

/*import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import androidx.core.content.ContextCompat;
//import com.fazpass.td.Fazpass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;*/


public class SimDataPlugin {
   /* private Context applicationContext;

    public SimDataPlugin(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    //sim data fetching method for android 10 or lower versions
    public CompletableFuture<JSONObject> getSimData1() {
        CompletableFuture<JSONObject> sims = CompletableFuture.supplyAsync(()->{
            SubscriptionManager subscriptionManager = (SubscriptionManager) this.applicationContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if(PackageManager.PERMISSION_GRANTED == ContextCompat
            .checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE) &&
            PackageManager.PERMISSION_GRANTED == ContextCompat
            .checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_NUMBERS)){
                List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
                JSONArray cards = new JSONArray();
                int i = 0;
                for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {

                    //storing sim data returned from sim card system service object into variables
                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                    CharSequence carrierName = subscriptionInfo.getCarrierName();
                    String countryIso = subscriptionInfo.getCountryIso();
                    int dataRoaming = subscriptionInfo.getDataRoaming();  // 1 is enabled ; 0 is disabled
                    CharSequence displayName = subscriptionInfo.getDisplayName();
                    String serialNumber = subscriptionInfo.getIccId();
                    boolean networkRoaming = subscriptionManager.isNetworkRoaming(slotIndex);
                    // String phoneNumber = subscriptionInfo.getNumber();
                    int subscriptionId = subscriptionInfo.getSubscriptionId();

                    //storing variable data into new json object for each sim card
                    JSONObject card = new JSONObject();

                    try {
                        card.put(Fazpass.Sim.CARRIER_NAME, carrierName.toString());
                        card.put(Fazpass.Sim.COUNTRY_CODE, countryIso);
                        card.put(Fazpass.Sim.DISPLAY_NAME, displayName.toString());
                        card.put(Fazpass.Sim.IS_DATA_ROAMING, (dataRoaming == 1));
                        card.put(Fazpass.Sim.IS_NETWORK_ROAMING, networkRoaming);
                        // card.put("phoneNumber", phoneNumber);
                        card.put(Fazpass.Sim.SERIAL_NUMBER, serialNumber);
                        card.put(Fazpass.Sim.SUBSCRIPTION_ID, subscriptionId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    try {
                        String phoneNumber = subscriptionInfo.getNumber();
                        card.put(Fazpass.Sim.PHONE_NUMBER, phoneNumber);
                    } catch (Exception ex) {
                        System.out.println("Excp - " + ex);
                    }
                    //add json object of sim card data into json array
                    cards.put(card);
                }

                //storing json array into another json object
                JSONObject simCards = new JSONObject();
                try {
                    simCards.put(Fazpass.Sim.CARDS, cards);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return data in json format object
                return simCards;
            }else{
                return new JSONObject();
            }
        });
       return sims;
    }

    //sim data fetching method for android 11 version
    public CompletableFuture<JSONObject> getSimData() {
        CompletableFuture<JSONObject> sims = CompletableFuture.supplyAsync(()->{
            TelecomManager tm2;
            Iterator<PhoneAccountHandle> phoneAccounts;
            PhoneAccountHandle phoneAccountHandle;
            //get phone service to access telephone network subscription or sim cards
            SubscriptionManager subscriptionManager = (SubscriptionManager) this.applicationContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if(PackageManager.PERMISSION_GRANTED == ContextCompat
            .checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE) &&
            PackageManager.PERMISSION_GRANTED == ContextCompat
            .checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_NUMBERS)){
                List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
                //get telephony manager service of device system
                TelephonyManager telephonyManager = (TelephonyManager) this.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
                // try{
                //get telecom manager service of device system
                tm2 = (TelecomManager) this.applicationContext.getSystemService(Context.TELECOM_SERVICE);
                //get all call capable phone accounts
                phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();

                JSONArray cards = new JSONArray();
                int count = 0;
                for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
                    //storing sim data returned from sim card system service object into variables
                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                    CharSequence carrierName = subscriptionInfo.getCarrierName();
                    String countryIso = subscriptionInfo.getCountryIso();
                    int dataRoaming = subscriptionInfo.getDataRoaming();  // 1 is enabled ; 0 is disabled
                    CharSequence displayName = subscriptionInfo.getDisplayName();
                    String serialNumber =subscriptionInfo.getIccId();
                    int mcc = subscriptionInfo.getMcc();
                    int mnc = subscriptionInfo.getMnc();
                    boolean networkRoaming = subscriptionManager.isNetworkRoaming(slotIndex);
                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                    JSONObject card = new JSONObject();

                    try {
                        card.put(Fazpass.Sim.CARRIER_NAME, carrierName.toString());
                        card.put(Fazpass.Sim.COUNTRY_CODE, countryIso);
                        card.put(Fazpass.Sim.DISPLAY_NAME, displayName.toString());
                        card.put(Fazpass.Sim.IS_DATA_ROAMING, (dataRoaming == 1));
                        card.put(Fazpass.Sim.IS_NETWORK_ROAMING, networkRoaming);
                        card.put(Fazpass.Sim.SUBSCRIPTION_ID,subscriptionId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try{
                        String phoneNumber = subscriptionInfo.getNumber();
                        card.put(Fazpass.Sim.PHONE_NUMBER,phoneNumber);
                    }catch(Exception ex){
                        System.out.println("Excp - "+ex);
                    }
                    phoneAccountHandle = phoneAccounts.next();
                    if(count==0){
                        try {
                            card.put(Fazpass.Sim.SERIAL_NUMBER,phoneAccountHandle.getId().substring(0,19));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            card.put(Fazpass.Sim.SERIAL_NUMBER,phoneAccountHandle.getId().substring(0,19));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    count++;
                    System.out.println("serial number - "+phoneAccountHandle.getId().substring(0,19));
                    cards.put(card);

                }

                //storing json array into another json object
                JSONObject simCards = new JSONObject();
                try {
                    simCards.put(Fazpass.Sim.CARDS, cards);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //return data in json format object
                return simCards;
            }else{
                return new JSONObject();
            }

        });
      return sims;
    }

*/
}
