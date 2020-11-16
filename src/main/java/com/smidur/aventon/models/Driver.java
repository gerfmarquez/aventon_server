package com.smidur.aventon.models;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * Copyright 2020, Gerardo Marquez.
 */

public class Driver {
    private String driverId;
    @SerializedName("syncDriverLocation")
    private Location driverLocation;
    private Passenger passenger;
    private String plates;
    private String makeModel;

    public Location getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(Location driverLocation) {
        this.driverLocation = driverLocation;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public String getPlates() {
        return plates;
    }

    public void setPlates(String plates) {
        this.plates = plates;
    }

    public String getMakeModel() {
        return makeModel;
    }

    public void setMakeModel(String makeModel) {
        this.makeModel = makeModel;
    }

    @Override
    public String toString() {
        return "Location: "+driverLocation+", Passenger: "+passenger+" Plates: "+plates+" Make: "+makeModel;
    }
    @Override
    public boolean equals(Object obj) {
        return obj!=null &&
                obj instanceof Driver &&
                driverId.equals(((Driver) obj).driverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId);
    }
}
