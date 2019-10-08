package graft;

public class Options {

    private static Config options;

    static void init(Config config, CLI cli) {
        if (options != null) {
            throw new GraftException("Options already initialized");
        }
        options = config.copy();
        // TODO: CLI args
    }

    public static Config v() {
        if (options == null) {
            throw new GraftException("Options not initialized");
        }
        return options;
    }

}
