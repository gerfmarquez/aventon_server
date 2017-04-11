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

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet(asyncSupported = true)
public class RideAvailabilityServlet extends HttpServlet {


    RideManager rideManager = RideManager.i();

    class Payload {
        String sub;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //todo fucking handle IOexception that makes the servlet restart over and over again!!!!
        //todo one step closer, updated aws java library. other than that i was using an invalid token so that's why getcredentials call for aws didn't work.
        // i have to handle internal server error that occurs when token is not valid. also prevent app from calling doget multiple times due to unexpected exceptions of aws


        try {
            AsyncContext asyncContext = req.startAsync();
            asyncContext.setTimeout(240000);
            rideManager.lookForRide(asyncContext,
                    extractDriver(req.getHeader("Authorization")),
                    null);
        } catch(TokenNotValidException tnve) {

        }


    }

    private String extractIdentifier(String authorizationParameter) throws TokenNotValidException {
        System.out.println("authorization "+authorizationParameter);
        String payload[] = authorizationParameter.split("\\.");
//        for(int i = 0; i < payload.length; i++) {
        System.out.println("tp decode payload "+ " ,"+payload[1]);
        String decodedPayload = new String(Base64.getDecoder().decode(payload[1].getBytes()));
        System.out.println("decoded payload "+ " ,"+decodedPayload);
//        }
        try {
            RideAvailabilityServlet.Payload payloadObject = new Gson().fromJson(decodedPayload, RideAvailabilityServlet.Payload.class);
            String cognitoIdentityId = payloadObject.sub;

            Map providerTokens = new HashMap();
            providerTokens.put("cognito-identity.amazonaws.com", authorizationParameter);
//        GettokenRequest.setLogins(providerTokens);

            AmazonCognitoIdentityClient identityClient = new AmazonCognitoIdentityClient();
            identityClient.setRegion(RegionUtils.getRegion(Configuration.REGION));
            GetCredentialsForIdentityRequest request = new GetCredentialsForIdentityRequest();
            request.withLogins(providerTokens);
            request.setIdentityId(cognitoIdentityId);
            GetCredentialsForIdentityResult tokenResp = identityClient.getCredentialsForIdentity(request);
            //todo handle exception internal server error when token is not valid!! yay!!
            System.out.println("Token: "+tokenResp);

            //todo get the driver from memory?
            return cognitoIdentityId;


        } catch(JsonIOException jsonExc) {
            jsonExc.printStackTrace();
        }
        throw new TokenNotValidException();
    }
    class TokenNotValidException extends Exception {

    }
    protected Driver extractDriver(String authorizationParameter) throws TokenNotValidException {
//        String[] url = req.getRequestURL().toString().split("/");
//        for(int i = 0; i < url.length;i++) {
//            System.out.println("User: "+ url[i].toString());
//        }
//        String authorizationParameter = authorization;
        return new Driver(extractIdentifier(authorizationParameter));

//        String driver = url[4].toString();
    }
}
