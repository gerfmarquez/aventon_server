package com.smidur.aventon.servlets;

import com.amazonaws.cognito.devauthsample.Configuration;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityResult;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet(asyncSupported = true)
public class RideAvailabilityServlet extends HttpServlet {


    RideManager rideManager = RideManager.i();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String[] url = req.getRequestURL().toString().split("/");
//        for(int i = 0; i < url.length;i++) {
//            System.out.println("User: "+ url[i].toString());
//        }
        Map providerTokens = new HashMap();
        providerTokens.put("cognito-identity.amazonaws.com", "auidhashaisdhals");
        tokenRequest.setLogins(providerTokens);

        AmazonCognitoIdentityClient identityClient = new AmazonCognitoIdentityClient();
        identityClient.setRegion(RegionUtils.getRegion(Configuration.REGION));
        GetCredentialsForIdentityRequest request = new GetCredentialsForIdentityRequest();
        request.withLogins(providerTokens);
        request.setIdentityId("us-east-1:XXXXX-9ac6-YYYY-ac07-ZZZZZZZZZZZZ");
        GetCredentialsForIdentityResult tokenResp = identityClient.getCredentialsForIdentity(request);


        String driver = url[4].toString();

        rideManager.lookForRide(
                req.startAsync(req,resp),
                new Driver(driver),
                null);

    }

}
