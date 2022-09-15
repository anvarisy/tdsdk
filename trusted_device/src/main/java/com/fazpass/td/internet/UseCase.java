package com.fazpass.td.internet;

import com.fazpass.td.internet.request.*;
import com.fazpass.td.internet.response.*;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UseCase {

    @POST("v1/trusted-device/check")
    Observable<Response<CheckUserResponse>> startService(@Header("Authorization")String token, @Body CheckUserRequest body);

    @POST("v1/trusted-device/enroll")
    Observable<Response<EnrollDeviceResponse>> enrollDevice(@Header("Authorization")String token, @Body EnrollDeviceRequest body);

    @POST("v1/trusted-device/verify")
    Observable<Response<ValidateDeviceResponse>> validateDevice(@Header("Authorization")String token, @Body ValidateDeviceRequest body);

    @POST("v1/trusted-device/remove")
    Observable<Response<RemoveDeviceResponse>> removeDevice(@Header("Authorization")String token, @Body RemoveDeviceRequest body);

}
