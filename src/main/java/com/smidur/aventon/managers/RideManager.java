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

    //todo make these two hashmap's keys be the Driver and passenger so they're unique
    HashMap<Driver,AsyncContext> driverAwaitingRide = new HashMap<>();
    HashMap<Passenger,AsyncContext> passengerAwaitingPickup = new HashMap<>();
    //todo also there should be a scheduled task that cleans up async contexts connections that are closed.


    void findDriversNearby(PassengerLocation passengerLocation) {}


    /**
     * Called by the Passenger when requesting a pickup.
     * @param passenger
     * @param acceptPickup the callback that the driver INDIRECTLY  calls when they {@link #confirmRide}
     */
    public void requestPickup(AsyncContext passengerAsync, Passenger passenger, AcceptPickup acceptPickup) {
        AsyncContext previousAsyncContext = passengerAwaitingPickup.put(passenger,passengerAsync);
        if(previousAsyncContext != null) {
            previousAsyncContext.complete();
        }

        onRideAvailable(passenger);
        //todo what happens if they're no rides available (nearby?) ? send fail error message?
    }

    public interface AcceptPickup {
        /**
         *
         * @param driver Driver that has confirmed the ride.
         */
        void onAcceptPickup(Driver driver);
    }
    private void onAcceptPickup(Driver driver, Passenger passenger) {
        for(Map.Entry<Passenger,AsyncContext> passengerEntry: passengerAwaitingPickup.entrySet()) {

            Passenger tempPassenger = passengerEntry.getKey();
            AsyncContext passengerAsync  = null;

            if(tempPassenger.equals(passenger)) {

                passengerAsync = passengerEntry.getValue();

                try {
                    ServletOutputStream outputStream = passengerAsync.getResponse().getOutputStream();
                    outputStream.println("Driver: "+driver.toString());
                    outputStream.flush();

                } catch(IllegalStateException ise){
                    //todo improve connection dropped logic
                    passengerAwaitingPickup.remove(passengerEntry.getKey());
                    ise.printStackTrace();
                } catch(Exception e){
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
        //todo check if the previous async context is closed before assigning the new one to the same Driver.

        AsyncContext previousAsyncContext = driverAwaitingRide.put(driver,asyncContext);
        if(previousAsyncContext != null) {
            previousAsyncContext.complete();
        }
    }

    public interface RideAvailable {
        /**
         * Note: Only deliver events to nearby drivers.
         * @param passenger Passenger that has requrested the pickup.
         */
        void onRideAvailable(Passenger passenger);
    }
    private void onRideAvailable(Passenger passenger) {
        for(Map.Entry<Driver,AsyncContext> driverEntry: driverAwaitingRide.entrySet()) {

            AsyncContext driverAsync = driverEntry.getValue();

            try {
                ServletOutputStream outputStream = driverAsync.getResponse().getOutputStream();
                outputStream.println("Passenger: "+passenger.toString());
                outputStream.flush();

            } catch(IllegalStateException ise){
                //todo improve connection dropped logic
                driverAwaitingRide.remove(driverEntry.getKey());
                ise.printStackTrace();
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
