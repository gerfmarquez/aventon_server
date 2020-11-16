package com.smidur.aventon.servlets;

import com.google.gson.Gson;
import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Passenger;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Level;

/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * Copyright 2020, Gerardo Marquez.
 */

@WebServlet(asyncSupported = true)
public class SchedulePickupServlet extends RootServlet {


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)  {

        if(req.getDispatcherType() == DispatcherType.ERROR) {
            //todo remove async context from Ride Manager
            logger.info("async context expired");
            return;
        }

        try {
            String passengerId = extractIdentifier(req.getHeader("Authorization"));

            BufferedReader reader = new BufferedReader(req.getReader());
            String tempLine;
            StringBuilder jsonBuilder = new StringBuilder();
            while((tempLine = reader.readLine()) != null) {
                jsonBuilder.append(tempLine);
            }
            Passenger passenger = new Gson().fromJson(jsonBuilder.toString(),Passenger.class);
            passenger.setPassengerId(passengerId);


            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(ASYNC_TIMEOUT);
            RideManager.i().requestPickup(asyncContext,passenger,
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
