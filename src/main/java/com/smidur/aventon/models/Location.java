package com.smidur.aventon.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marqueg on 2/8/17.
 */
public class Location {

    @SerializedName("syncLocationLatitude")
    private double latitude;

    @SerializedName("syncLocationLongitude")
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
