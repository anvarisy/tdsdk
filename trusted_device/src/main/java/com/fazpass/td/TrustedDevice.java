package com.fazpass.td;

import io.reactivex.rxjava3.core.Observable;

public abstract class TrustedDevice extends BASE{

    public abstract Observable<Fazpass> check(String email, String phone);

}
