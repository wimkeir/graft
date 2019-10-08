package graft;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple command line interface that parses and validates an array of arguments.
 *
 * @author Wim Keirsgieter
 */
class CLI {

    private static Logger log = LoggerFactory.getLogger(CLI.class);

    // TODO: validate args in constructor
    // TODO: debug args to console if debug enabled

    private CommandLine cmd;

    /**
     * Initialize a new CLI instance with the given argument array.
     *
     * @param args the command line arguments
     * @throws GraftException if the arguments cannot be parsed
     */
    CLI(String[] args) throws GraftException {
        org.apache.commons.cli.Options options = initOpts();
        CommandLineParser parser = new DefaultParser();

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.error("Error parsing command line arguments", e);
            throw new GraftException("Error parsing command line arguments");
        }
    }

    /**
     * Check if the CLI contains the given option.
     *
     * @param opt the option to check for
     * @return true if the CLI contains the option, else false
     */
    boolean hasOpt(String opt) {
        return cmd.hasOption(opt);
    }

    /**
     * Get the given option from the CLI.
     *
     * @param opt the option to get
     * @return the value of the option, or null if it does not exist
     */
    String getOpt(String opt) {
        return cmd.getOptionValue(opt);
    }

    /**
     * Get the given option from the CLI, or a default value if it doesn't exist
     *
     * @param opt the option to get
     * @param defaultValue the default value
     * @return the value of the option, or the default value if it doesn't exist
     */
    String getOpt(String opt, String defaultValue) {
        return cmd.getOptionValue(opt, defaultValue);
    }

    // set up the command line options
    private org.apache.commons.cli.Options initOpts() {
        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        options.addOption(new Option("config",  true, "path to config file"));
        options.addOption(new Option("class", true, "path to class file to analyse"));
        options.addOption(new Option("dir", true, "path to directory to analyse"));
        options.addOption(new Option("jar", true, "path to JAR to analyse"));
        return options;
    }

}
