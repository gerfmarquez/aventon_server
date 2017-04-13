package com.smidur.aventon.servlets;

import com.amazonaws.cognito.devauthsample.Configuration;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityResult;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.Passenger;
import com.smidur.aventon.utils.Log;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet(asyncSupported = true)
public class RideAvailabilityServlet extends RootServlet {


    RideManager rideManager = RideManager.i();



    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)  {

        if(req.getDispatcherType() == DispatcherType.ERROR) {
            //todo remove async context from Ride Manager
            logger.info("async context expired");
            return;
        }

        try {

            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(ASYNC_TIMEOUT);
            rideManager.lookForRide(asyncContext,
                    extractDriver(req.getHeader("Authorization")),
                    null);

        } catch(TokenNotValidException tnve) {
            logger.log(Level.WARNING,"Token not valid or expired.",tnve);
        } catch(Exception e) {
            logger.log(Level.SEVERE,"This shouldn't happen",e);
        }


    }


}
