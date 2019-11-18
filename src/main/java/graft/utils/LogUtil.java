package graft.utils;

import ch.qos.logback.classic.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graft.GraftRuntimeException;

import static graft.Const.*;

/**
 * Miscellaneous utility functions for working with logging libraries.
 */
public class LogUtil {

    /**
     * Set the root logging level of the project.
     *
     * @param logLevel the log level to set
     */
    public static void setLogLevel(String logLevel) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        switch(logLevel) {
            case NONE:
                root.detachAppender("stdout");
                break;
            case TRACE:
                root.setLevel(Level.TRACE);
                break;
            case DEBUG:
                root.setLevel(Level.DEBUG);
                break;
            case INFO:
                root.setLevel(Level.INFO);
                break;
            case WARN:
                root.setLevel(Level.WARN);
                break;
            case ERROR:
                root.setLevel(Level.ERROR);
                break;
            case ALL:
                root.setLevel(Level.ALL);
                break;
            default:
                throw new GraftRuntimeException("Unrecognised log level '" + logLevel + "'");
        }
    }

}
