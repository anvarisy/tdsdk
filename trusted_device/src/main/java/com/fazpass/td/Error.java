package com.fazpass.td;

public class Error {

    public static NullPointerException localDataMissing(){
        return new NullPointerException(Fazpass.LOCAL_MISSING);
    }

    public static SecurityException pinNotMatch(){
        return new SecurityException(Fazpass.PIN_NOT_MATCH);
    }
}
