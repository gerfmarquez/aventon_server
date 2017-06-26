package com.smidur.aventon.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marqueg on 6/18/17.
 */
public class Origin {
    @SerializedName("originAddress")
    String address;
    @SerializedName("originLocation")
    Location location;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
