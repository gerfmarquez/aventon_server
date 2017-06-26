package com.smidur.aventon.servlets;


import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.Location;


import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet(asyncSupported = true)
public class RideAvailabilityServlet extends RootServlet {




    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)  {

        if(req.getDispatcherType() == DispatcherType.ERROR) {
            //todo remove async context from Ride Manager
            logger.info("async context expired");
            return;
        }

        try {

            String driverIdentifier = extractIdentifier(req.getHeader("Authorization"));
            Driver driver = new Driver();

            driver.setDriverId(driverIdentifier);
//            driver.setDriverLocation(new Location());

            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(ASYNC_TIMEOUT);
            RideManager.i().lookForRide(asyncContext,
                    driver,
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
