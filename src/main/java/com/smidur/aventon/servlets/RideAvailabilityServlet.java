package com.smidur.aventon.servlets;

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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet
public class RideAvailabilityServlet extends HttpServlet {


    RideManager rideManager = RideManager.i();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);

        rideManager.lookForRide(
                req.startAsync(req,resp),
                new Driver("driver1"),
                null);

    }

}
