package graft;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

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

    /**
     * The path to the default configuration resource
     */
    private static final String DEFAULT_CONFIG_RESOURCE = "default.properties";

    private static Logger log = LoggerFactory.getLogger(Config.class);

    private Configuration configuration;

    // ********************************************************************************************
    // private constructors
    // ********************************************************************************************

    private Config() {
        this.configuration = new PropertiesConfiguration();
    }

    private Config(Configuration configuration) {
        this.configuration = configuration;
    }

    // ********************************************************************************************
    // public instance methods
    // ********************************************************************************************

    public Config copy() {
        Configuration copy = new PropertiesConfiguration();
        Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            copy.addProperty(key, configuration.getProperty(key));
        }
        return new Config(copy);
    }

    public void amend(Config other) {
        Iterator<String> keys = other.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            configuration.setProperty(key, other.getProperty(key));
        }
    }

    public Config subset(String prefix) {
        return new Config(configuration.subset(prefix));
    }

    public List<Object> getList(String key) {
        return configuration.getList(key);
    }

    public Iterator<String> keys() {
        return configuration.getKeys();
    }

    public Iterator<String> keys(String prefix) {
        return configuration.getKeys(prefix);
    }

    public boolean containsKey(String key) {
        return configuration.containsKey(key);
    }

    // property access methods

    public Object getProperty(String key) {
        return configuration.getProperty(key);
    }

    public String getString(String key) {
        return configuration.getString(key);
    }

    public String getString(String key, String def) {
        return configuration.getString(key, def);
    }

    public String[] getStringArray(String key) {
        return configuration.getStringArray(key);
    }

    public int getInt(String key) {
        return configuration.getInt(key);
    }

    public int getInt(String key, int def) {
        return configuration.getInt(key, def);
    }

    public boolean getBoolean(String key) {
        return configuration.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean def) {
        return configuration.getBoolean(key, def);
    }

    public void setProperty(String key, Object value) {
        configuration.setProperty(key, value);
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
                throw new GraftException("Could not create new file '" + filename + "'");
            }
            toFile(file);
        } catch (IOException e) {
            throw new GraftException("Cannot write config to file '" + filename + "'", e);
        }
    }

    private void toFile(File file) throws IOException {
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

    // ********************************************************************************************
    // public static methods
    // ********************************************************************************************

    static Config getDefault() {
        URL url = ClassLoader.getSystemClassLoader().getResource(DEFAULT_CONFIG_RESOURCE);
        assert url != null;
        try {
            File file = new File(url.toURI());
            return fromFile(file);
        } catch (URISyntaxException|GraftException e) {
            throw new GraftException("Could not load default config file");
        }
    }

    static Config fromFile(File file) {
        try {
            Configuration configuration = new PropertiesConfiguration(file);
            return new Config(configuration);
        } catch (ConfigurationException e) {
            throw new GraftException("Cannot read config file '" + file.getName() + "'");
        }
    }

    static Config fromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            throw new GraftException("No such config file '" + filename + "'");
        }
        if (!file.isFile()) {
            throw new GraftException("Cannot open config file '" + filename + "'");
        }
        return fromFile(file);
    }

    static Config fromFileWithDefaults(String filename) {
        Config def = getDefault();
        Config conf = fromFile(filename);
        def.amend(conf);
        return def;
    }

}
