package graft;

import java.util.Iterator;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graft.Const.NONE;

/**
 * A wrapper around the Apache Configuration object, with static methods for reading and validating
 * configuration files.
 *
 * @author Wim Keirsgieter
 */
public class GraftConfig {

    private static Logger log = LoggerFactory.getLogger(GraftConfig.class);

    // TODO: this won't work unless the src dir is present (which it won't be)
    private static String DEFAULT_CONFIG_PATH = "src/main/resources/config.xml";
    private static Configurations configHelper = new Configurations();

    private Configuration config;
    private String fileName;

    private GraftConfig(String fileName, Configuration config) {
        this.fileName = fileName;
        this.config = config;
    }

    private GraftConfig(Configuration config) {
        this.fileName = NONE;
        this.config = config;
    }

    // ********************************************************************************************
    // public instance methods
    // ********************************************************************************************

    /**
     * Get the object property specified by the given key.
     *
     * @param key the property key
     * @return the property value
     */
    public Object getProperty(String key) {
        return config.getProperty(key);
    }

    /**
     * Get the string property specified by the given key.
     *
     * @param key the property key
     * @return the property value
     */
    public String getString(String key) {
        return config.getString(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(configFile = ").append(fileName).append(")\n");

        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            sb.append(key).append(" = ").append(config.getProperty(key)).append("\n");
        }
        sb.delete(sb.lastIndexOf("\n"), sb.length());

        return sb.toString();
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    /**
     * Read the user config file.
     *
     * @param fileName the config file name
     * @return the configuration
     * @throws GraftException if the config file is invalid or cannot to be read
     */
    public static GraftConfig fromFile(String fileName) throws GraftException {
        log.debug("Reading configuration file '{}'", fileName);

        try {
            Configuration config = configHelper.xml(fileName);
            if (validConfig(config)) {
                addDefaults(config);
                return new GraftConfig(fileName, config);
            } else {
                log.debug("Invalid configuration file '{}'", fileName);
                throw new GraftException("Configuration file '{}' is invalid");
            }
        } catch (ConfigurationException e) {
            log.error("Error reading config file '{}'", fileName, e);
            throw new GraftException("Unable to read config file: " + e.getMessage());
        }
    }

    /**
     * Get the default configuration.
     *
     * @return the default configuration
     * @throws GraftException if the default config file cannot be read
     */
    public static GraftConfig defaultConfig() throws GraftException {
        return new GraftConfig(getDefaultConfig());
    }

    // ********************************************************************************************
    // private methods
    // ********************************************************************************************

    private static boolean validConfig(Configuration config) {
        // TODO
        return true;
    }

    private static Configuration getDefaultConfig() throws GraftException {
        try {
            Configuration defaultConfig = configHelper.xml(DEFAULT_CONFIG_PATH);
            assert validConfig(defaultConfig);
            return defaultConfig;
        } catch (ConfigurationException e) {
            log.error("Error reading default config file ({})", DEFAULT_CONFIG_PATH, e);
            throw new GraftException("Unable to read default config file");
        }
    }

    private static void addDefaults(Configuration config) throws GraftException {
        Configuration defaultConfig = getDefaultConfig();
        Iterator<String> keys = defaultConfig.getKeys();

        while (keys.hasNext()) {
            String key = keys.next();
            if (!config.containsKey(key)) {
                config.setProperty(key, defaultConfig.getProperty(key));
            }
        }
    }

}
