package com.fazpass.td.internet;

import com.fazpass.td.internet.request.*;
import com.fazpass.td.internet.response.*;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UseCase {

    @POST("/check")
    Observable<Response<CheckUserResponse>> startService(@Header("Authorization")String token, @Body CheckUserRequest body);

    @POST("/enroll")
    Observable<Response<EnrollDeviceResponse>> enrollDevice(@Header("Authorization")String token, @Body EnrollDeviceRequest body);

    @POST("/validate")
    Observable<Response<ValidateDeviceResponse>> validateDevice(@Header("Authorization")String token, @Body ValidateDeviceRequest body);

    @POST("/remove")
    Observable<Response<RemoveDeviceResponse>> removeDevice(@Header("Authorization")String token, @Body RemoveDeviceRequest body);

    @POST("/recover")
    Observable<Response<RecoverAccountResponse>> recoverDevice(@Header("Authorization")String token, @Body RecoverAccountRequest body);

}
