package com.smidur.aventon.servlets;

import com.google.gson.Gson;
import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.Location;
import com.smidur.aventon.models.Passenger;
import com.smidur.aventon.models.RideSummary;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by marqueg on 8/14/17.
 */
@WebServlet(asyncSupported = true)
public class CompleteRideServlet extends RootServlet{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(req.getDispatcherType() == DispatcherType.ERROR) {
            //todo remove async context from Ride Manager
            logger.info("async context expired");
            return;
        }

        try {
            Passenger passenger = new Passenger();
            passenger.setPassengerId(req.getParameter("passengerId"));


            String driverIdentifier = extractIdentifier(req.getHeader("Authorization"));

            //todo add driver validation
            Driver driver = new Driver();
            driver.setDriverId(driverIdentifier);


            try {

                String jsonString = readJsonFromInput(req.getInputStream());
                RideSummary rideSummary = new Gson().fromJson(jsonString, RideSummary.class);

                RideManager.i().completeRide(driver, passenger,rideSummary);

            } catch(IOException readInputException) {
                logger.log(Level.WARNING,"JSON Format Exception",readInputException);
                resp.sendError(500,"JSON Format Exception");
                return;
            }



        } catch(TokenNotValidException tnve) {
            logger.log(Level.WARNING,"Token not valid or expired.",tnve);
            try {
                resp.sendError(401,"Not  Authorized");
                if(req.isAsyncStarted()) {
                    req.getAsyncContext().complete();
                }
                return;
            } catch(IOException ioe) {
                logger.log(Level.WARNING,"Error sending response code.",ioe);
            }


        } catch(Exception e) {
            logger.log(Level.SEVERE,"This shouldn't happen",e);
        }

    }

}
