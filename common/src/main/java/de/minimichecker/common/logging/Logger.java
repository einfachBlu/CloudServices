package de.minimichecker.common.logging;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface Logger {

    String COLOR_ERROR_START = "\u001b[" + Color.RED.getLoggingColorId() + "m";
    String COLOR_ERROR_END = "\u001b[m";

    Set<Logger> loggers = new HashSet<>();

    /**
     * Get the Global Logger
     */
    static Logger getGlobal() {
        return Logger.loggers.iterator().next();
    }

    /**
     * Get all registered Loggers
     */
    static Collection<Logger> getLoggers() {
        return Logger.loggers;
    }

    /**
     * Initialize Logging with setting the Directory
     * and saving the Log files there
     *
     * @param logsDirectory the Directory where the logs
     *                      should be stored
     */
    void init(File logsDirectory);

    /**
     * logs a normal text into the console
     *
     * @param message the message which should print into the console
     */
    void info(String message);

    /**
     * logs an error text into the console
     *
     * @param message the message which should print into the console
     */
    void error(String message);

    /**
     * logs an error text and exception into the console
     *
     * @param message   the message which should print into the console
     * @param throwable the Exception which will be thrown
     */
    void error(String message, Throwable throwable);

    /**
     * logs a debug text into the console
     *
     * @param message the message which should print into the console
     */
    void debug(String message);

    /**
     * logs a warning text into the console
     *
     * @param message the message which should print into the console
     */
    void warning(String message);
}