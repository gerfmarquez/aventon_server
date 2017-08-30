package com.smidur.aventon.models;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Created by marqueg on 2/8/17.
 */
public class Passenger {

    @SerializedName("syncPassengerId")
    private String passengerId;


    @SerializedName("syncDestination")
    Destination destination;

    @SerializedName("syncOrigin")
    Origin origin;


    private Driver driver;



    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }


    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "Origin: "+origin+" Destination: "+destination;
    }

    @Override
    public boolean equals(Object obj) {
        return obj!=null &&
                obj instanceof Passenger &&
                passengerId.equals(((Passenger) obj).passengerId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(passengerId);
    }
}
