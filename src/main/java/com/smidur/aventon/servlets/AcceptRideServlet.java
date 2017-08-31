package com.smidur.aventon.servlets;

import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.Passenger;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by marqueg on 2/7/17.
 */

public class AcceptRideServlet extends RootServlet  {


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

            RideManager.i().confirmRide(
                    driver,
                    passenger,

                    null);


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
