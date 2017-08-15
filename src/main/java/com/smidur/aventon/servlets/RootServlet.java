package com.smidur.aventon.servlets;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.cognito.devauthsample.Configuration;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityResult;
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.smidur.aventon.models.Driver;
import com.smidur.aventon.models.Passenger;
import com.smidur.aventon.utils.Log;

import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by marqueg on 4/5/17.
 */
public class RootServlet extends HttpServlet {

    Logger logger = Log.getLogger();

    protected static final int ASYNC_TIMEOUT = 4 * 60 * 1000;



    protected String extractIdentifier(String authorizationParameter) throws TokenNotValidException {

        try {

//            logger.info("authorization " + authorizationParameter);
            String payload[] = authorizationParameter.split("\\.");

//            logger.info("payload "+ " ,"+payload[1]);

            String decodedPayload = new String(Base64.getDecoder().decode(payload[1].getBytes()));

//            logger.info("decoded payload "+ " ,"+decodedPayload);

            Payload payloadObject = new Gson().fromJson(decodedPayload,
                    Payload.class);

            String cognitoIdentityId = payloadObject.sub;

            Map providerTokens = new HashMap();
            providerTokens.put("cognito-identity.amazonaws.com", authorizationParameter);


            AmazonCognitoIdentityClient identityClient = new AmazonCognitoIdentityClient();
            identityClient.setRegion(RegionUtils.getRegion(Configuration.REGION));

            GetCredentialsForIdentityRequest request = new GetCredentialsForIdentityRequest();
            request.withLogins(providerTokens);
            request.setIdentityId(cognitoIdentityId);

            try {
                GetCredentialsForIdentityResult tokenResp = identityClient.getCredentialsForIdentity(request);


//               logger.info("Valid Token: "+tokenResp.getCredentials());

                return cognitoIdentityId;

            } catch(NotAuthorizedException nae) {
                throw new TokenNotValidException(nae);
            } catch(AmazonServiceException ase) {
                throw new TokenNotValidException(ase);
            }


        } catch(JsonIOException|ArrayIndexOutOfBoundsException jsonExc) {
            logger.log(Level.SEVERE,jsonExc,null);
            throw new TokenNotValidException(jsonExc);
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


    class TokenNotValidException extends Exception {
        public TokenNotValidException(Throwable cause) {
            super(cause);
        }
        public TokenNotValidException() {
            super();
        }
    }

    class Payload {
        String sub;
    }
}
