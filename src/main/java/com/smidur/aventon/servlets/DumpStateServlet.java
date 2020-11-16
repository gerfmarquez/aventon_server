package com.smidur.aventon.servlets;

import com.google.gson.Gson;
import com.smidur.aventon.Application;
import com.smidur.aventon.managers.RideManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * Copyright 2020, Gerardo Marquez.
 */

public class DumpStateServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        Properties prop = new Properties();

        //load a properties file from class path, inside static method
        prop.load(Application.class.getClassLoader().getResourceAsStream("key.properties"));



        String pass = req.getParameter("pass");
        if(pass != null && pass.contains(prop.getProperty("password"))) {
//            String mode = req.getParameter("mode");
//            if(mode != null && mode.contains("drive")) {
            String driversState = new Gson().toJson(RideManager.i().driverAwaitingRide.keySet());
            resp.getOutputStream().print(driversState);
//            }
//            else if (mode != null && mode.contains("passenger")) {
//                String driversState = new Gson().toJson(RideManager.i().passengerAwaitingPickup.keySet());
//                resp.getOutputStream().print(driversState);
//            }

        } else {
            resp.getOutputStream().print("{}");
        }
        resp.setStatus(200);
        resp.setContentType("application/json");

    }
}
