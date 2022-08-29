package com.fazpass.td;

public class Error {

    public static NullPointerException localDataMissing(){
        return new NullPointerException(TD.LOCAL_MISSING);
    }

    public static SecurityException pinNotMatch(){
        return new SecurityException(TD.PIN_NOT_MATCH);
    }
}
