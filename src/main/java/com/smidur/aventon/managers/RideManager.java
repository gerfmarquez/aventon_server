package com.smidur.aventon.managers;

import com.google.gson.Gson;
import com.smidur.aventon.models.*;
import com.smidur.aventon.utils.Constants;
import com.smidur.aventon.utils.LocationUtils;
import com.smidur.aventon.utils.Log;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    Logger logger = Log.getLogger();



    //todo make these two hashmap's keys be the Driver and passenger so they're unique
    public volatile Hashtable<Driver,AsyncContext> driverAwaitingRide = new Hashtable<>();
    public Hashtable<Passenger,AsyncContext> passengerAwaitingPickup = new Hashtable<>();
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
                previousAsyncContext.complete();//todo remove? since client now closes connection
            } catch(IllegalStateException ise) {
                //don't do anything
                logger.log(Level.WARNING,"Request Pickup async context illegal exception.");
                //todo log analytics
            }
        }
        //only if driver is scheduling a pickup for first time.
        onRideAvailable(passenger);
        //todo second time should verify first connection is open? or




    }

    public interface AcceptPickup {
        /**
         *
         * @param driver Driver that has confirmed the ride.
         */
        void onAcceptPickup(Driver driver);
    }
    private void onAcceptPickup(Driver requestDriver, Passenger requestPassenger) {

        //check if passenger taken already by another driver.
        for(Map.Entry<Driver,AsyncContext> driverEntry: driverAwaitingRide.entrySet()) {

            Driver checkOtherDriversAssignedPassenger = driverEntry.getKey();
            //don't notify drivers who are trying to
            if(!requestDriver.equals(checkOtherDriversAssignedPassenger)
                    && checkOtherDriversAssignedPassenger.getPassenger() != null
                    && checkOtherDriversAssignedPassenger.getPassenger().equals(requestPassenger)) {
                //inform driver of unsuccessful ride assignment
                try {
                    AsyncContext driverAsync = driverAwaitingRide.get(requestDriver);
                    ServletOutputStream outputStream = driverAsync.getResponse().getOutputStream();

                    outputStream.println("Taken: ");
                    logger.log(Level.FINE
                            ,"Ride Taken");
                    outputStream.flush();

                } catch(IllegalStateException ise) {
                    ise.printStackTrace();
                    driverAwaitingRide.remove(checkOtherDriversAssignedPassenger);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return;
            }

        }

        for(Map.Entry<Passenger,AsyncContext> passengerEntry: passengerAwaitingPickup.entrySet()) {

            Passenger loadedPassenger = passengerEntry.getKey();
            AsyncContext passengerAsync  = null;

            if(loadedPassenger.equals(requestPassenger)) {

                passengerAsync = passengerEntry.getValue();


                try {


                    for(Map.Entry<Driver,AsyncContext> loadedDriver: driverAwaitingRide.entrySet()) {

                        if(loadedDriver.getKey().equals(requestDriver)) {
                            loadedDriver.getKey().setPassenger(loadedPassenger);

                            //notify passenger of successful ride assignment
                            try {
                                ServletOutputStream outputStream = passengerAsync.getResponse().getOutputStream();
                                String json = new Gson().toJson(loadedDriver.getKey()).replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");
                                outputStream.println("Driver: "+json);
                                outputStream.flush();

                                logger.log(Level.FINE
                                        ,"Driver Assigned to Passenger");

                            }  catch(IllegalStateException ise){
                                logger.log(Level.WARNING,"Request Pickup async context illegal exception.");
                                passengerAwaitingPickup.remove(passengerEntry.getKey());
                                ise.printStackTrace();
                            }

                            //notify driver of successful ride assignment
                            try {
                                AsyncContext driverAsyncContext = loadedDriver.getValue();
                                ServletOutputStream outputStream = driverAsyncContext.getResponse().getOutputStream();
                                outputStream.println("Confirmed: ");
                                outputStream.flush();
                                logger.log(Level.FINE
                                        ,"Confirmed Ride by Driver");
                            } catch(IllegalStateException ise) {
                                driverAwaitingRide.remove(loadedDriver.getKey());
                                logger.log(Level.WARNING
                                        ,"Async Context Illegal while Confirming Ride by Driver");
                            }

                            break;
                        }

                    }




                } catch(Exception e){
                    logger.log(Level.WARNING
                            ,"Exception while Accepting Pickup.",e);
                }
//                passengerAsync.complete();
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
    private void onRideAvailable(Passenger requestPassenger) {

        //check if passenger taken already by any driver.
        //this check is edge case and would happen if schedule service on android app
        //
        for(Map.Entry<Driver,AsyncContext> driverEntry: driverAwaitingRide.entrySet()) {

            Driver driver = driverEntry.getKey();
            if(driver.getPassenger() != null && driver.getPassenger().equals(requestPassenger)) {
                logger.log(Level.WARNING
                        ,"Edge Case Passenger Taken, inbetween schedule call long-pool refresh.");
                return;//passenger taken so dont notify any drivers anymore
            }

        }

        boolean atLeastOneDriverNotified = false;
        for(Map.Entry<Driver,AsyncContext> driverEntry: driverAwaitingRide.entrySet()) {

            AsyncContext driverAsync = driverEntry.getValue();
            Driver driver = driverEntry.getKey();

            Location passengerLocation = requestPassenger.getOrigin().getLocation();
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

            String message = "Passenger: "+new Gson().toJson(requestPassenger);
            notifyMessageUserThroughAsync(driverAwaitingRide,driverAsync,message,driverEntry.getKey(),true);

            logger.log(Level.FINE
                    ,"Passenger Available");

        }
        if(!atLeastOneDriverNotified) {
            AsyncContext passengerAsync = passengerAwaitingPickup.get(requestPassenger);
            String message = "NoDriverFound: "+requestPassenger.toString();
            notifyMessageUserThroughAsync(passengerAwaitingPickup,passengerAsync,message,requestPassenger,false);
            passengerAwaitingPickup.remove(requestPassenger);

            logger.log(Level.FINE
                    ,"No Drivers Found Nearby.");
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


    public void updateDriverLocation(Location driverLocation, String driverIdentifier) {


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
                    notifyMessageUserThroughAsync(passengerAwaitingPickup,passengerAsync,message,newUpdatedDriver.getPassenger(),true);
                }

            }

        }


    }

    public void completeRide(Driver driver, RideSummary rideSummary) {


        for(Map.Entry<Driver,AsyncContext> tempDriverSet: driverAwaitingRide.entrySet()) {
            if(tempDriverSet.getKey().equals(driver)) {
                Driver loadedDriver = tempDriverSet.getKey();

                Passenger assignedPassenger = loadedDriver.getPassenger();
                //check is the intended passenger
                if(assignedPassenger != null && assignedPassenger.getPassengerId().equals(rideSummary.getPassengerId())) {

                    for(Map.Entry<Passenger,AsyncContext> passengerEntry: passengerAwaitingPickup.entrySet()) {
                        if(passengerEntry.getKey().equals(assignedPassenger)) {

                            String rideSummaryJson = new Gson().toJson(rideSummary).replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");;

                            notifyMessageUserThroughAsync(passengerAwaitingPickup,passengerEntry.getValue(),
                                    "DropOff: "+rideSummaryJson, passengerEntry.getKey(),false);

                            logger.log(Level.FINE
                                    ,"Passenger Drop Off");

                            //we don't need the passenger anymore
                            passengerAwaitingPickup.remove(passengerEntry.getKey());
                            loadedDriver.setPassenger(null);
                            break;
                        }

                    }

                }

                notifyMessageUserThroughAsync(driverAwaitingRide,tempDriverSet.getValue(),"Completed:",
                        tempDriverSet.getKey(),false);

                logger.log(Level.FINE
                        ,"Notified of Completed Ride To Driver");

                break;
            }

        }


    }


    private void notifyMessageUserThroughAsync(Hashtable asyncContextHashMap,AsyncContext asyncContext,
                                               String message, Object keyToRemoveIfOld, boolean keepConnection)  {
        try {
            ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
            outputStream.println(message);
            outputStream.flush();

        } catch(IllegalStateException ise){
            logger.log(Level.WARNING
                    ,"Expired Async Context before Notifying");
            asyncContextHashMap.remove(keyToRemoveIfOld);

        } catch(Exception e){
            logger.log(Level.WARNING
                    ,"Exception while notifying.",e);
        }
        if(!keepConnection) {
            asyncContext.complete();
        }
    }

}
