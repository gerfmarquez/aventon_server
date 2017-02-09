package com.smidur.aventon.managers;

import com.smidur.aventon.models.*;

/**
 * Created by marqueg on 2/8/17.
 */
public class RideManager {

    void findDriversNearby(PassengerLocation passengerLocation) {}


    /**
     * Called by a Driver when looking for rides.
     * @param driver
     * @param rideAvailable the callback that the passenger INDIRECTLY calls when they {@link #requestPickup}.
     */
    void lookForRide(Driver driver, RideAvailable rideAvailable) {}

    interface RideAvailable {
        /**
         * Note: Only deliver events to nearby drivers.
         * @param passenger Passenger that has requrested the pickup.
         */
        void onRideAvailable(Passenger passenger);
    }

    /**
     * Called by the Passenger when requesting a pickup.
     * @param passenger
     * @param acceptPickup the callback that the driver INDIRECTLY  calls when they {@link #confirmRide}
     */
    void requestPickup(Passenger passenger, AcceptPickup acceptPickup) {}

    interface AcceptPickup {
        /**
         *
         * @param driver Driver that has confirmed the ride.
         */
        void onAcceptPickup(Driver driver);
    }

    /**
     * Called by the Driver when they confirm the ride.
     * @param driver
     * @param onRideAssigned Called after the Passenger has received the {@link AcceptPickup} callback.
     */
    void confirmRide(Driver driver, AssignRide onRideAssigned) {}

    interface AssignRide {
        /**
         * todo Note: Unique ID add to hashmap Dynamo?
         * @param ride Ride with assigned driver and passenger.
         */
        void onRideAssigned(Ride ride);
    }
}
