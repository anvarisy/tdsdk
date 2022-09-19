# Fazpass-Android (Trusted Device)

This is the Official Android wrapper/library for Fazpass Trusted Device, that is compatible with Gradle.
Visit https://fazpass.com for more information about the product and see documentation at http://docs.fazpass.com for more technical details.

## Installation
Gradle
```groovy
allprojects {
	repositories {
	...
	maven {
           url "https://jitpack.io"
            // You can obtain by contacting us
           credentials { username authToken } 
           }
	}
}
```

```
 implementation 'com.github.fazpass:fazpass-android-trusted-device-sdk:TAG'
```

## Permission
As default this SDK used these permissions
```xml
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.USE_BIOMETRIC"/>
```
So make sure you request all of these permission as a requirement.

## Usage
Choose mode that u want to use ex: STAGING or PRODUCTION
```java
    Fazpass.initialize(Context, MERCHANT_KEY, TD_MODE.STAGING)
        .check("EMAIL","PHONE", new TrustedDeviceListner<Fazpass>(){
            @Override
            public void onSuccess(Fazpass f) {
                if(f.status.equals(TD_STATUS.KEY_IS_MATCH)){
                    //TODO
                }
            
            @Override
            public void onFailure(Throwable err) {
            
            }
        });
```
check will return some status with this detail

| Status                    | Detail            |
| -------------             | :-------------    |
| KEY_LOCALE_NOT_FOUND      | This user already registered in server and the trusted key also detected for this app but we cannot find trusted key in this device     |
| KEY_SERVER_NOT_FOUND      | This user already registered in server but the trusted key cannot be found for this app    |
| KEY_READY_TO_COMPARE      | CANNOT BE USE          |
| KEY_NOT_MATCH             | This user already registered in server and the trusted key also detected for this app but key in server is not match with key inside the device           |
| KEY_IS_MATCH              | everything is ok, you can continue          |
| USER_NOT_FOUND            | User haven't registered          |

## Function
We have some function after you call check method

| Method                    | Detail            |
| -------------             |:-------------:    |
| enrollDeviceByFinger      | registered new device with finger as a authentication  |
| enrollDeviceByPin         | registered new device with pin as a authentication     |
| validateUser              | validating data user between local and server          |
| removeDevice              | remove trusted status from this app inside this device |

### Validation
For validation function we will return confidence rate of this user in this device
```java
f.validateUser(pin.getText().toString(), new TrustedDeviceListener<ValidateStatus>() {
    @Override
    public void onSuccess(ValidateStatus result) {
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
    }

    @Override
    public void onFailure(Throwable err) {
        c.closeDialog();
        binding.tvDetail.setText(err.getMessage());
        }
    });
```
