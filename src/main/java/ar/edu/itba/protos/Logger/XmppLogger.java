package ar.edu.itba.protos.Logger;

import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Logger;
/**
 * Created by sebastian on 10/23/16.
 */
public class XmppLogger {

    private static XmppLogger instance;
    private Logger logger;

    private XmppLogger() throws IOException {
        logger = Logger.getLogger(XmppLogger.class);
        logger.addAppender(new FileAppender(new HTMLLayout(), "logs.html"));
    }

    public static XmppLogger getInstance() {
        try {
            if (instance == null)
                instance = new XmppLogger();
        } catch (IOException e) {
            System.out.println("Error opening the logger");
        }
        return instance;
    }

    public void info(Object message) {
        System.out.println("INFO: " + message);
        logger.info(message);
    }

    public void error(Object message) {
        System.out.println("ERROR: " + message);
        logger.error(message);
    }

}
