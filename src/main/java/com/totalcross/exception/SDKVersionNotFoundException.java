package com.totalcross.exception;

public class SDKVersionNotFoundException extends Exception {
    String version;
    public SDKVersionNotFoundException (String version) {
        this.version = version;
    }

    @Override
    public String getMessage() {
        return "TotalCross SDK Version " + version + " Not Found. Insert a valid totalcross-sdk version and try again";
    }
}
