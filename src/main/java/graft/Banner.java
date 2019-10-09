package graft;

public class Banner {

    private static final int DEFAULT_WIDTH = 100;

    private StringBuilder sb = new StringBuilder();
    private int width;

    public Banner() {
        this(DEFAULT_WIDTH);
    }

    public Banner(int width) {
        this.width = width;
        for (int i = 0; i < width; i++) sb.append('=');
        sb.append(String.format("\n| %1$-" + (width - 4) + "s |\n", " "));
    }

    public void println(String s) {
        sb.append(String.format("| %1$-" + (width - 4) + "s |\n", s));
    }

    public void display() {
        sb.append(String.format("| %1$-" + (width - 4) + "s |\n", " "));
        for (int i = 0; i < width; i++) sb.append('=');
        System.out.println(sb.toString());
    }

}
