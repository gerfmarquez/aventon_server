package com.smidur.aventon.servlets;

import com.smidur.aventon.managers.RideManager;

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
public class SchedulePickupServlet extends RootServlet {

    RideManager rideManager = RideManager.i();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {

        if(req.getDispatcherType() == DispatcherType.ERROR) {
            //todo remove async context from Ride Manager
            logger.info("async context expired");
            return;
        }

        try {

            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(ASYNC_TIMEOUT);
            rideManager.requestPickup(asyncContext,
                    extractPassenger(req.getHeader("Authorization")),
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
