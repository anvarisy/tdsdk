package com.fazpass.td;

public class EnrollStatus {
    private final boolean status;

    private final String message;

    public EnrollStatus(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
