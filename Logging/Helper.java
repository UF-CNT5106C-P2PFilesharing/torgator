package Logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * This class is used to generate log files to write messages into
 */
public class Helper {
    //Handler to write in the specified file
    public FileHandler fileHandler;

    //Instance used to log messages
    public static Logger log = Logger.getLogger(Helper.class.getName());

    /**
     * This method is used to initialize logging configuration.
     * It creates new log file and sets file handler to write messages into
     * @param currentPeerID - peerID of which log file needs to be created
     */
    public void initializeLogger(String currentPeerID) {
        try {
            fileHandler = new FileHandler("log_peer_" + currentPeerID + ".log");
            fileHandler.setFormatter(new LogFormatter());
            log.addHandler(fileHandler);
            log.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to log a message in a log file and show it in console
     * @param message - message to be logged and showed in console
     */
    public static void logAndDisplayInConsole(String message) {
        log.info(message);
        System.out.println(LogFormatter.getFormattedMessage(message));
    }
}