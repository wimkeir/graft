package graft;

/**
 * Basic banner for terminal display.
 *
 * @author Wim Keirsgieter
 */
public class Banner {

    private static int DEFAULT_WIDTH = 100;

    private StringBuilder sb;
    private int width;

    // ********************************************************************************************
    // constructors
    // ********************************************************************************************

    public Banner() {
        this(DEFAULT_WIDTH);
    }

    public Banner(int width) {
        this(width, "");
    }

    public Banner(String heading) {
        this(DEFAULT_WIDTH, heading);
    }

    public Banner(int width, String heading) {
        this.width = width;
        sb = new StringBuilder();

        border();
        println();

        int lenHeading = heading.length();
        if (lenHeading > 0) {
            println(heading);
            println();
        }
    }

    // ********************************************************************************************
    // instance methods
    // ********************************************************************************************

    /**
     * Print an empty line to the banner.
     */
    public void println() {
        sb.append(String.format("| %1$-" + (width - 4) + "s |\n", " "));
    }

    /**
     * Print the given string to the banner.
     *
     * @param s the string to print
     */
    public void println(String s) {
        sb.append(String.format("| %1$-" + (width - 4) + "s |\n", s));
    }

    /**
     * Display the banner to stdout.
     */
    public void display() {
        println();
        border();
        System.out.println(sb.toString());
    }

    private void border() {
        for (int i = 0; i < width; i++) {
            sb.append('=');
        }
        sb.append('\n');
    }

}
