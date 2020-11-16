package com.smidur.aventon.servlets;

import com.google.gson.Gson;
import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.Location;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Level;

/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * Copyright 2020, Gerardo Marquez.
 */

public class UpdateLocationServlet extends RootServlet {




    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp)  {


        if(req.getDispatcherType() == DispatcherType.ERROR) {
            //todo remove async context from Ride Manager
            logger.info("async context expired");
            return;
        }

        try {

            String driverIdentifier = extractIdentifier(req.getHeader("Authorization"));


            try {

                String jsonString = readJsonFromInput(req.getInputStream());
                Location driverLocation = new Gson().fromJson(jsonString, Location.class);

                RideManager.i().updateDriverLocation(driverLocation, driverIdentifier);

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
