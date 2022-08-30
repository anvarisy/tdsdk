package com.fazpass.td;

public class Error {

    public static NullPointerException localDataMissing(){
        return new NullPointerException(BASE.LOCAL_MISSING);
    }

    public static SecurityException pinNotMatch(){
        return new SecurityException(BASE.PIN_NOT_MATCH);
    }
}
