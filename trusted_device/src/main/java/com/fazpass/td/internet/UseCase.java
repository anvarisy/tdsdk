package com.fazpass.td.internet;

import com.fazpass.td.internet.request.CheckUserRequest;
import com.fazpass.td.internet.request.EnrollDeviceRequest;
import com.fazpass.td.internet.request.EnrollUserRequest;
import com.fazpass.td.internet.response.CheckUserResponse;
import com.fazpass.td.internet.response.EnrollDeviceResponse;
import com.fazpass.td.internet.response.EnrollUserResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UseCase {

    @POST("/check")
    Observable<Response<CheckUserResponse>> startService(@Header("Authorization")String token, @Body CheckUserRequest body);

    @POST("/enroll-user")
    Observable<Response<EnrollUserResponse>> registerUser(@Header("Authorization")String token, @Body EnrollUserRequest body);

    @POST("/enroll-device/")
    Observable<EnrollDeviceResponse> enrollDevice(@Header("Authorization")String token, @Body EnrollDeviceRequest body);
}
