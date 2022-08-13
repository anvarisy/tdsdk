package com.fazpass.td;

public interface EnrollDevice<T> {
    void enrollDeviceSuccess(T result);
    void enrollDeviceFailure(Throwable err);
}
