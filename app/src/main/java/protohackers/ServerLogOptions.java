package protohackers;

import java.util.logging.*;

public class ServerLogOptions
{
    public static final Level DEBUG = new Level("DEBUG", Level.FINE.intValue() + 1)
    {};
    private final Logger logger;

    public ServerLogOptions(Logger logger)
    {
        this.logger = logger;
    }

    public void debug(String msg) // cyan
    {
        logger.log(DEBUG, msg);
    }

    public void info(String msg) // green
    {
        logger.info(msg);
    }

    public void warning(String msg) // yellow
    {
        logger.warning(msg);
    }

    public void error(String msg) // red
    {
        logger.severe(msg);
    }
}
