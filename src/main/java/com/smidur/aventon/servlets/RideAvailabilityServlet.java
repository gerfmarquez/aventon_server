package com.smidur.aventon.servlets;


import com.google.gson.Gson;
import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.DriverInfo;
import com.smidur.aventon.models.Location;
import com.smidur.aventon.models.Passenger;


import javax.servlet.*;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet(asyncSupported = true)
public class RideAvailabilityServlet extends RootServlet {




    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)  {

        if(req.getDispatcherType() == DispatcherType.ERROR) {
            //todo remove async context from Ride Manager
            logger.info("async context expired");
            return;
        }

        try {

            String driverIdentifier = extractIdentifier(req.getHeader("Authorization"));
            Driver driver = new Driver();

            driver.setDriverId(driverIdentifier);

            BufferedReader reader = new BufferedReader(req.getReader());
            String tempLine;
            StringBuilder jsonBuilder = new StringBuilder();
            while((tempLine = reader.readLine()) != null) {
                jsonBuilder.append(tempLine);
            }
            DriverInfo driverInfo = new Gson().fromJson(jsonBuilder.toString(),DriverInfo.class);
            driver.setMakeModel(driverInfo.getMakeModel());
            driver.setPlates(driverInfo.getPlates());


            AsyncContext asyncContext = req.startAsync();

            //timeout/expire/discard old connections per driver.
            expireOldDriverConnections(asyncContext);

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

    private void expireOldDriverConnections(AsyncContext asyncContext)  {
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                System.out.println("On Complete");
                for(final Map.Entry<Driver,AsyncContext> driverEntry: RideManager.i().driverAwaitingRide.entrySet()) {

                    final AsyncContext previousDriverAsync = driverEntry.getValue();
                    if(previousDriverAsync != null &&
                            previousDriverAsync.equals(asyncEvent.getAsyncContext())) {

                        logger.log(Level.FINE,"async context was the same on memory");

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Driver checkExpiredDriverKey = driverEntry.getKey();
                                if(RideManager.i().driverAwaitingRide.containsKey(checkExpiredDriverKey)) {

                                    AsyncContext checkExpiredAsync =
                                            RideManager.i().driverAwaitingRide.get(checkExpiredDriverKey);

                                    if(checkExpiredAsync.equals(previousDriverAsync)) {
                                        //todo driver might have a pending ride to finish so notify error?
                                        RideManager.i().driverAwaitingRide.remove(checkExpiredDriverKey);
                                    }
                                }
                            }
                        },ASYNC_TIMEOUT);
                    }

                }

            }


            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
            }
        });
    }
}
