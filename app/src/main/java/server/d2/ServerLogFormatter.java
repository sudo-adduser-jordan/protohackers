
package server.d2;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ServerLogFormatter extends Formatter {
    public static final String RESET = "\u001b[0m";
    public static final String RED = "\u001b[31m";
    public static final String YELLOW = "\u001b[33m";
    public static final String BLUE = "\u001b[34m";
    public static final String GREEN = "\u001b[32m";

    public static Logger getLogger(Class<?> inputClass) 
    {
        Logger logger = Logger.getLogger(inputClass.getName());
        logger.setUseParentHandlers(false); // Disable default handlers

        if (logger.getHandlers().length == 0) {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new ServerLogFormatter()); // Your custom formatter
            logger.addHandler(handler);
        }
        return logger;
    };

    @Override
    public String format(LogRecord record) {
        String timestamp = String.format("%1$tF %1$tT", new java.util.Date(record.getMillis()));

        String levelColor;
        String levelStr = record.getLevel().toString();
        switch (levelStr) {
            case "SEVERE":
                levelColor = RED;
                break;
            case "WARNING":
                levelColor = YELLOW;
                break;
            case "INFO":
            default:
                levelColor = GREEN;
                break;
        }

        String levelFormatted = RESET + "[" + levelColor + levelStr + RESET + "]" ;
        String loggerName = record.getLoggerName();
        String message = record.getMessage();

        // Format: 2024-04-27 12:34:56 [LEVEL] loggerName: message
        return String.format("%s %s %s: %s%n",
                BLUE + timestamp,
                levelFormatted,
                loggerName,
                GREEN + message
        );
    }
}