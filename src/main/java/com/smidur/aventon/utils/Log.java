package com.smidur.aventon.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by marqueg on 4/12/17.
 */
public class Log {

    private static Logger logger;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Get the logger for AWSCognitoDeveloperAuthenticationSample
     */

    public static synchronized Logger getLogger() {
        if (null != logger) {
            return logger;
        }

        logger = Logger.getLogger("AventonLog");
        FileHandler handler;
        try {
            handler = new FileHandler("aventon.log", true);

            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);

            logger.addHandler(handler);
            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logger. Aborting.", e);
        }

        return logger;
    }
    private Log() {
    }

}
