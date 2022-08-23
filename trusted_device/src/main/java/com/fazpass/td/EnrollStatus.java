package com.fazpass.td;

public class EnrollStatus {
    private boolean status;

    private String message;

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
