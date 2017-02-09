package com.smidur.aventon.servlets;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by marqueg on 2/7/17.
 */
@WebServlet
public class RideAvailabilityServlet extends HttpServlet {

    Timer timer = new Timer("ClientNotifier");


    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        final AsyncContext aCtx = req.startAsync(req, res);
        aCtx.setTimeout(3* 60 * 1000);
        System.out.println("Async Timeout:"+aCtx.getTimeout());
        // Suspend request for 30 Secs
        timer.schedule(new TimerTask() {

            public void run() {
                try{

                    //read unread alerts count
//                        int unreadAlertCount = alertManager.getUnreadAlerts();
                    // write unread alerts count
                    ServletOutputStream outputStream = aCtx.getResponse().getOutputStream();
                    outputStream.println("Hello");
                    outputStream.flush();

                }
                catch(Exception e){
                    e.printStackTrace();
                }
                aCtx.complete();
            }
        }, (3 * 60 * 1000) - (5 * 1000));

    }

}
