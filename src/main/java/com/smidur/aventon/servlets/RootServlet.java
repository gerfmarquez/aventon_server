package com.smidur.aventon.servlets;

import com.amazonaws.cognito.devauthsample.Configuration;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityResult;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.Passenger;

import javax.servlet.http.HttpServlet;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marqueg on 4/5/17.
 */
public class RootServlet extends HttpServlet {

    protected Driver extractDriver(String authorizationParameter) throws TokenNotValidException {
//        String[] url = req.getRequestURL().toString().split("/");
//        for(int i = 0; i < url.length;i++) {
//            System.out.println("User: "+ url[i].toString());
//        }
//        String authorizationParameter = authorization;
        return new Driver(extractIdentifier(authorizationParameter));

//        String driver = url[4].toString();
    }
    protected Passenger extractPassenger(String authorizationParameter) throws TokenNotValidException {
        return new Passenger(extractIdentifier(authorizationParameter));
    }


    private String extractIdentifier(String authorizationParameter) throws TokenNotValidException{
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
}
