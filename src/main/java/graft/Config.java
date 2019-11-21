package graft;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graft.Const.*;

/**
 * A wrapper around the Apache Commons Configuration, with helpful static methods.
 *
 * @author Wim Keirsgieter
 */
public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);

    private Configuration configuration;

    // ********************************************************************************************
    // private constructors
    // ********************************************************************************************

    private Config(Configuration configuration) {
        this.configuration = configuration;
    }

    // ********************************************************************************************
    // instance methods
    // ********************************************************************************************

    /**
     * Create a copy of the Config object.
     *
     * @return a copy of the Config object
     */
    public Config copy() {
        Configuration copy = new PropertiesConfiguration();
        Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            copy.addProperty(key, configuration.getProperty(key));
        }
        return new Config(copy);
    }

    /**
     * Combine this Config with another Config object, overwriting any duplicate fields with values
     * from the other Config.
     *
     * @param other the Config object to combine with this one
     */
    public void combine(Config other) {
        Iterator<String> keys = other.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            configuration.setProperty(key, other.getProperty(key));
        }
    }

    /**
     * Get an Iterator of the keys in this Config object.
     *
     * @return an Iterator of property keys
     */
    public Iterator<String> keys() {
        return configuration.getKeys();
    }

    /**
     * Check whether this Config contains the given key.
     *
     * @param key the key to check for membership
     * @return true if the Config contains a value for the key, else false
     */
    public boolean containsKey(String key) {
        return configuration.containsKey(key);
    }

    // property access methods

    /**
     * Returns the given property if it exists.
     *
     * @param key the property key
     * @return the property value
     */
    public Object getProperty(String key) {
        checkContains(key);
        return configuration.getProperty(key);
    }

    /**
     * Set the given property value.
     *
     * @param key the property key
     * @param value the property value
     */
    public void setProperty(String key, Object value) {
        configuration.setProperty(key, value);
    }

    /**
     * Get the given string property if it exists.
     *
     * @param key the property key
     * @return the property value
     */
    public String getString(String key) {
        checkContains(key);
        return configuration.getString(key);
    }

    /**
     * Debug the properties of the Config object.
     */
    public void debug() {
        if (log.isDebugEnabled()) {
            log.debug("Running with options:");
            Iterator<String> keys = keys();
            while (keys.hasNext()) {
                String key = keys.next();
                log.debug(" - {}: {}", key, getProperty(key));
            }
        }
    }

    /**
     * Write the Config object to a properties file.
     *
     * @param path the path to the file
     */
    public void toFile(Path path) {
        try {
            toFile(path.toFile());
        } catch (IOException e) {
            throw new GraftRuntimeException("Cannot write config to file '" + path.toFile().getName() + "'", e);
        }
    }

    private void toFile(File file) throws IOException {
        if (file.exists()) {
            log.warn("File '{}' already exists, overwriting", file.getName());
        }

        FileWriter out = new FileWriter(file);
        out.write(PROPERTIES_HEADER);

        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            String key = keys.next();
            out.write(key + " = " + getProperty(key) + "\n");
        }

        out.close();
    }

    private void checkContains(String key) {
        if (!containsKey(key)) {
            throw new GraftRuntimeException("No value set for key '" + key + "'");
        }
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    /**
     * Get the default configuration.
     *
     * @return the default configuration
     */
    static Config getDefault() {
        URL url = ClassLoader.getSystemClassLoader().getResource(DEFAULT_CONFIG_RESOURCE);
        assert url != null;
        try {
            File file = new File(url.toURI());
            return fromFile(file);
        } catch (URISyntaxException| GraftRuntimeException e) {
            throw new GraftRuntimeException("Could not load default config file");
        }
    }

    /**
     * Load a configuration from a properties file.
     *
     * @param file the properties file
     * @return the configuration
     */
    static Config fromFile(File file) {
        try {
            Configuration configuration = new PropertiesConfiguration(file);
            return new Config(configuration);
        } catch (ConfigurationException e) {
            throw new GraftRuntimeException("Cannot read config file '" + file.getName() + "'");
        }
    }

    /**
     * Load a configuration from a properties file.
     *
     * @param filename the properties file
     * @return the configuration
     */
    static Config fromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            throw new GraftRuntimeException("No such config file '" + filename + "'");
        }
        if (!file.isFile()) {
            throw new GraftRuntimeException("Cannot open config file '" + filename + "'");
        }
        return fromFile(file);
    }

    /**
     * Load a configuration from a properties file with defaults.
     *
     * @param filename the properties file
     * @return the configuration
     */
    static Config fromFileWithDefaults(String filename) {
        Config def = getDefault();
        Config conf = fromFile(filename);
        def.combine(conf);
        return def;
    }

    // ********************************************************************************************
    // overridden Object methods
    // ********************************************************************************************

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Config:\n");

        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            String key = keys.next();
            sb.append(key).append(": ").append(getProperty(key)).append("\n");
        }

        return sb.toString();
    }

}
