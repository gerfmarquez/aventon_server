package com.smidur.aventon.models;

/**
 * Created by marqueg on 2/8/17.
 */
public class Passenger {
    String passengerId;
    PassengerLocation passengerLocation;
    private Passenger() {
        //NOP
    }
    public Passenger(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    @Override
    public String toString() {
        return passengerId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj!=null &&
                obj instanceof Passenger &&
                passengerId.equals(((Passenger) obj).passengerId);

    }
}
