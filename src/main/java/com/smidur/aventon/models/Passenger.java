package com.smidur.aventon.models;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * Copyright 2020, Gerardo Marquez.
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
