package orebfuscator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log 
{
    private static boolean configured;
    private static Logger myLog;

    private static void configureLogging()
    {
        myLog = LogManager.getLogger(Orebfuscator.MODID);
        configured = true;
    }

    public static void log(Level level, String format, Object... data)
    {
        if (!configured)
        {
            configureLogging();
        }
        myLog.log(level, String.format(format, data));
    }
    
    public static void msg(String format, Object... data)
    {
    	log(Level.INFO, format, data);
    }

    public static void error(String format, Object... data)
    {
    	log(Level.ERROR, format, data);
    }
}
