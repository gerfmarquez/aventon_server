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

/**
 * Created by marqueg on 5/25/17.
 */
public class UpdateLocationServlet extends RootServlet {




    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp)  {

        System.out.println("size of hashmap ### "+RideManager.i().driverAwaitingRide.size()+" obj: "+RideManager.i());

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
    protected String readJsonFromInput(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder json = new StringBuilder();
        String line = null;
        while((line= reader.readLine())!= null) {
            json.append(line);
        }
        return json!=null && !json.toString().isEmpty()?json.toString():null;
    }

}
