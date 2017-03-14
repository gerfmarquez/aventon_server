package com.smidur.aventon.managers;

import com.smidur.aventon.models.*;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by marqueg on 2/8/17.
 */
public class RideManager {

    private static RideManager i;
    private RideManager() {}
    public static RideManager i() {
        if(i==null)i=new RideManager();
        return i;
    }

    HashMap<String,Ride> rideHashMap = new HashMap<>();

    HashMap<AsyncContext,Driver> driverAwaitingRide = new HashMap<>();
    HashMap<AsyncContext,Passenger> passengerAwaitingPickup = new HashMap<>();


    void findDriversNearby(PassengerLocation passengerLocation) {}


    /**
     * Called by the Passenger when requesting a pickup.
     * @param passenger
     * @param acceptPickup the callback that the driver INDIRECTLY  calls when they {@link #confirmRide}
     */
    public void requestPickup(AsyncContext passengerAsync, Passenger passenger, AcceptPickup acceptPickup) {

        passengerAwaitingPickup.put(passengerAsync, passenger);

        onRideAvailable(passenger);
    }

    public interface AcceptPickup {
        /**
         *
         * @param driver Driver that has confirmed the ride.
         */
        void onAcceptPickup(Driver driver);
    }
    private void onAcceptPickup(Driver driver, Passenger passenger) {
        for(Map.Entry<AsyncContext,Passenger> passengerEntry: passengerAwaitingPickup.entrySet()) {

            Passenger tempPassenger = passengerEntry.getValue();
            AsyncContext passengerAsync  = null;

            if(tempPassenger.equals(passenger)) {

                passengerAsync = passengerEntry.getKey();

                try {
                    ServletOutputStream outputStream = passengerAsync.getResponse().getOutputStream();
                    outputStream.println("Driver: "+driver.toString());
                    outputStream.flush();

                } catch(Exception e){
                    //todo is connection dropped?
                    e.printStackTrace();
                }
                passengerAsync.complete();
            }



        }
    }

    /**
     * Called by a Driver when looking for rides.
     * @param driver
     * @param rideAvailable the callback that the passenger INDIRECTLY calls when they {@link #requestPickup}.
     */
    public void lookForRide(AsyncContext asyncContext,Driver driver, RideAvailable rideAvailable) {
        driverAwaitingRide.put(asyncContext,driver);
    }

    public interface RideAvailable {
        /**
         * Note: Only deliver events to nearby drivers.
         * @param passenger Passenger that has requrested the pickup.
         */
        void onRideAvailable(Passenger passenger);
    }
    private void onRideAvailable(Passenger passenger) {
        for(Map.Entry<AsyncContext,Driver> driverEntry: driverAwaitingRide.entrySet()) {

            AsyncContext driverAsync = driverEntry.getKey();

            try {
                ServletOutputStream outputStream = driverAsync.getResponse().getOutputStream();
                outputStream.println("Passenger: "+passenger.toString());
                outputStream.flush();

            } catch(IllegalStateException ise){
                driverAwaitingRide.remove(driverEntry.getKey());
            } catch(Exception e){
                e.printStackTrace();
            }
            driverAsync.complete();

        }
    }


    /**
     * Called by the Driver when they confirm the ride.
     * @param driver
     * @param onRideAssigned Called after the Passenger has received the {@link AcceptPickup} callback.
     */
    public void confirmRide(Driver driver, Passenger passenger, AssignRide onRideAssigned) {


        //todo validate and assign a ride, driver and passenger.
        onAcceptPickup(driver, passenger);


    }

    public interface AssignRide {
        /**
         * todo Note: Unique ID add to hashmap Dynamo?
         * @param ride Ride with assigned driver and passenger.
         */
        void onRideAssigned(Ride ride);
    }






}
