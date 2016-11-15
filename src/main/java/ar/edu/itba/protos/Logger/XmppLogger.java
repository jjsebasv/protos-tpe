package ar.edu.itba.protos.Logger;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sebastian on 10/23/16.
 */
public class XmppLogger {

    private static XmppLogger instance;
    private Logger logger;

    /**
     * Set a log with type INFO
     * @param message
     */
    public void info(String message) {
        System.out.println("INFO: " + message);
        logger.info(message);
    }

    /**
     * Set a log with type ERROR
     * @param message
     */
    public void error(String message) {
        System.out.println("ERROR: " + message);
        logger.error(message);
    }

    /**
     * Set a log with type WARNING
     * @param message
     */
    public void warn(String message) {
        System.out.println("WARNING: " + message);
        logger.warn(message);
    }

    private XmppLogger() throws IOException {
        logger = LoggerFactory.getLogger(XmppLogger.class);
    }

    public static XmppLogger getInstance() {
        try {
            if (instance == null)
                instance = new XmppLogger();
        } catch (IOException e) {
            XmppLogger.getInstance().error("Error opening the logger");
        }
        return instance;
    }

}
