package com.fazpass.td;

public interface TrustedDeviceListener<T> {
    void onSuccess(T result);
    void onFailure(Throwable err);
}
