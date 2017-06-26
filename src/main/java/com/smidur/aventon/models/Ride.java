package com.smidur.aventon.models;

/**
 * Created by marqueg on 2/8/17.
 */
public class Ride {

    Driver driver;
    Passenger passenger;

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }
}
