package com.fazpass.td;

import io.reactivex.rxjava3.core.Observable;

public abstract class TrustedDevice extends BASE{

    public abstract void check(String email, String phone, TrustedDeviceListener<Fazpass> enroll);

}
