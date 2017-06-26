package com.smidur.aventon.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by marqueg on 6/18/17.
 */
public class Destination {
    @SerializedName("destinationAddress")
    String address;
    @SerializedName("destinationLocation")
    Location location;

}
