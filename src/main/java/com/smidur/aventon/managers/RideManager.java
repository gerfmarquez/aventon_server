package com.smidur.aventon.managers;

import com.google.gson.Gson;
import com.smidur.aventon.models.*;
import com.smidur.aventon.utils.Constants;
import com.smidur.aventon.utils.LocationUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
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
    public volatile Hashtable<Driver,AsyncContext> driverAwaitingRide = new Hashtable<>();
    Hashtable<Passenger,AsyncContext> passengerAwaitingPickup = new Hashtable<>();
    //todo also there should be a scheduled task that cleans up async contexts connections that are closed.


    void findDriversNearby(Location location) {}


    /**
     * Called by the Passenger when requesting a pickup.
     * @param passenger
     * @param acceptPickup the callback that the driver INDIRECTLY  calls when they {@link #confirmRide}
     */
    public void requestPickup(AsyncContext passengerAsync, Passenger passenger, @Deprecated AcceptPickup acceptPickup) {

        AsyncContext previousAsyncContext = passengerAwaitingPickup.put(passenger,passengerAsync);

        if(previousAsyncContext != null) {
            //kill old connection
            try {
                previousAsyncContext.complete();
            } catch(IllegalStateException ise) {
                //don't do anything
            }
        } else {
            //only if driver is scheduling a pickup for first time.
            onRideAvailable(passenger);
            //todo second time should verify first connection is open? or
        }



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

                    for(Map.Entry<Driver,AsyncContext> tempDriver: driverAwaitingRide.entrySet()) {
                        if(tempDriver.getKey().equals(driver)) {
                            tempDriver.getKey().setPassenger(passenger);
                        }
                        //todo un-assign passenger from driver once ride finishes
                        ///or let it expire
                    }

                    //todo inform driver if  no  passenger  was found


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
    public void lookForRide(AsyncContext asyncContext,Driver driver, @Deprecated RideAvailable rideAvailable) {
        //todo check if the previous async context is closed before assigning the new one to the same Driver.

        AsyncContext previousAsyncContext = driverAwaitingRide.put(driver,asyncContext);
        System.out.println("size of hashmap %%% "+driverAwaitingRide.size()+" obj: "+RideManager.this);
        //kill old connection
        try {
            if(previousAsyncContext!=null) {
                previousAsyncContext.complete();
            }
        } catch(IllegalStateException ise) {
            //don't do anything
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
        boolean atLeastOneDriverNotified = false;
        for(Map.Entry<Driver,AsyncContext> driverEntry: driverAwaitingRide.entrySet()) {

            AsyncContext driverAsync = driverEntry.getValue();
            Driver driver = driverEntry.getKey();

            Location passengerLocation = passenger.getOrigin().getLocation();
            Location driverLocation = driver.getDriverLocation();//todo driver location
            if(driverLocation == null) {
                continue;//skip driver
            }

            float distanceTo = LocationUtils.distanceBetween(
                    passengerLocation.getLatitude(),
                    passengerLocation.getLongitude(),
                    driverLocation.getLatitude(),
                    driverLocation.getLongitude()
            );

            if(distanceTo > Constants.PICKUP_THRESHOLD) {
                continue;//don't notify drivers that are not nearby
            }
            atLeastOneDriverNotified = true;

            String message = "Passenger: "+new Gson().toJson(passenger);
            notifyMessageUserThroughAsync(driverAsync,message,driverEntry.getKey(),false);

        }
        if(!atLeastOneDriverNotified) {
            AsyncContext passengerAsync = passengerAwaitingPickup.get(passenger);
            String message = "NoDriverFound: "+passenger.toString();
            notifyMessageUserThroughAsync(passengerAsync,message,passenger,false);
            passengerAwaitingPickup.remove(passenger);
        }

        //todo setup a timer if no drivers accept the ride?
        //todo what happens if there are no rides available (nearby?) ? send fail error message?
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


    public void updateDriverLocation(Location driverLocation, String driverIdentifier) {

        System.out.println("size of hashmap $$$ "+driverAwaitingRide.size()+" obj: "+RideManager.this);


        //todo fix instead of iterating every driver find a better way
        for(Map.Entry<Driver,AsyncContext> tempDriver: driverAwaitingRide.entrySet()) {
            Driver newUpdatedDriver = tempDriver.getKey();
            if(newUpdatedDriver.getDriverId().equals(driverIdentifier)) {
                //update latest location of the driver on the server side
                newUpdatedDriver.setDriverLocation(driverLocation);


                //if driverdoesn't have a passenger yet then dont send update of driver location to passenger

                if(newUpdatedDriver.getPassenger()!=null) {
                    AsyncContext passengerAsync = passengerAwaitingPickup.get(newUpdatedDriver.getPassenger());
                    String message = "NewDriverLocation: "
                            +newUpdatedDriver.getDriverLocation().getLatitude()
                            +","
                            +newUpdatedDriver.getDriverLocation().getLongitude();
                    notifyMessageUserThroughAsync(passengerAsync,message,newUpdatedDriver.getPassenger(),true);
                }

            }
            //todo un-assign passenger from driver once ride finishes
            ///or let it expire
        }


    }

    private void notifyMessageUserThroughAsync(AsyncContext asyncContext,String message, Object keyToRemoveIfOld,boolean keepConnection)  {
        try {
            ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
            outputStream.println(message);//todo send json
            outputStream.flush();

        } catch(IllegalStateException ise){
            //todo improve connection dropped logic
            driverAwaitingRide.remove(keyToRemoveIfOld);
            ise.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
        if(!keepConnection) {
            asyncContext.complete();
        }
    }

}
