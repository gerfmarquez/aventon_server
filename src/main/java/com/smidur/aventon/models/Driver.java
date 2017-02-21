package com.smidur.aventon.models;

/**
 * Created by marqueg on 2/8/17.
 */
public class Driver {
    String driverId;
    DriverLocation driverLocation;

    public Driver(String driverId) {
        this.driverId = driverId;
    }
    public DriverLocation getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(DriverLocation driverLocation) {
        this.driverLocation = driverLocation;
    }

    @Override
    public String toString() {
        return driverId;
    }
    @Override
    public boolean equals(Object obj) {
        return obj!=null &&
                obj instanceof Driver &&
                driverId.equals(((Driver) obj).driverId);
    }
}
