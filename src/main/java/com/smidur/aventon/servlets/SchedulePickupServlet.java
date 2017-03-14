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

        String[] url = req.getRequestURL().toString().split("/");
        for(int i = 0; i < url.length;i++) {
            String user = url[i];
            System.out.println("User: "+ user.toString());
        }

        rideManager.requestPickup(
                req.startAsync(req,resp),
                new Passenger("Passenger1"),
                null);
    }
}
