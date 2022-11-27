package Logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class handles the formatting of messages in log files
 */
public class LogFormatter extends Formatter {
    //Format in which date should be written
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    @Override
    public String format(LogRecord record) {
        return getFormattedMessage(record.getMessage());
    }

    public static String getFormattedMessage(String message) {
        return dateTimeFormatter.format(LocalDateTime.now()) + ": " + "peerProcess " + message + "\n";
    }
}