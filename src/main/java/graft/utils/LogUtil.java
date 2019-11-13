package graft.utils;

import ch.qos.logback.classic.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graft.Const.*;

public class LogUtil {

    // TODO: throw exception on default, javadoc

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
                // TODO
        }
    }

}
