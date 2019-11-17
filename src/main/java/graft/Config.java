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

    // TODO
    // handle misc todos, comments
    // javadocs

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

    public Object getProperty(String key) {
        checkContains(key);
        return configuration.getProperty(key);
    }

    public void setProperty(String key, Object value) {
        configuration.setProperty(key, value);
    }

    public String getString(String key) {
        checkContains(key);
        return configuration.getString(key);
    }

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

    public void toFile(String filename) {
        File file = new File(filename);
        try {
            if (file.exists()) {
                // TODO: ensure we're actually overwriting and not just appending
                log.warn("Config file '{}' already exists, overwriting...");
            }
            if (!file.createNewFile()) {
                throw new GraftRuntimeException("Could not create new file '" + filename + "'");
            }
            toFile(file);
        } catch (IOException e) {
            throw new GraftRuntimeException("Cannot write config to file '" + filename + "'", e);
        }
    }

    private void toFile(File file) throws IOException {
        if (file.exists()) {
            log.warn("File '{}' already exists, overwriting", file.getName());
        }

        FileWriter out = new FileWriter(file);
        out.write(PROPERTIES_HEADER);

        // TODO: this won't handle arrays and ref types
        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            String key = keys.next();
            out.write(key + " = " + getProperty(key) + "\n");
        }

        // TODO NB XXX
        // This resource isn't closed on error paths in the enclosing try-catch block in toFile(String)
        // See if we can actually pick this up with Graft?
        out.close();
    }

    private void checkContains(String key) {
        if (!containsKey(key)) {
            throw new GraftRuntimeException("No value set for key '" + key + "'");
        }
    }

    public void toFile(Path path) {
        try {
            toFile(path.toFile());
        } catch (IOException e) {
            throw new GraftRuntimeException("Cannot write config to file '" + path.toFile().getName() + "'", e);
        }
    }

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

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

    static Config fromFile(File file) {
        try {
            Configuration configuration = new PropertiesConfiguration(file);
            return new Config(configuration);
        } catch (ConfigurationException e) {
            throw new GraftRuntimeException("Cannot read config file '" + file.getName() + "'");
        }
    }

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
