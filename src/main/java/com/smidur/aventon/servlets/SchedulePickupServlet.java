package com.smidur.aventon.servlets;

import com.smidur.aventon.managers.RideManager;
import com.smidur.aventon.models.Passenger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet(asyncSupported = true)
public class SchedulePickupServlet extends HttpServlet {

    RideManager rideManager = RideManager.i();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

//
//        try {
//            rideManager.requestPickup(
//                    req.startAsync(req,resp),
//                    extractPassenger(req.getHeader("Authorization")),
//                    null);
//        } catch(TokenNotValidException tnve) {
//            resp.sendError(401,"Unauthorized Access");
//        }
    }


}
