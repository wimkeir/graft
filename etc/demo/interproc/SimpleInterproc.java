public class SimpleInterproc {

    public static void main(String[] args) {
        int b = bar();

        if (b < 10) {
            sanitize(b);
        }

        foo(b);
    }

    public static void foo(int c) {
        sink(c);
    }

    public static int bar() {
        int c = source();
        return c;
    }

    public static void sink(int b) {
        // a sensitive sink...
    }

    public static int source() {
        return (int) Math.random();
    }

    public static void sanitize(int b) {
        // sanitize the variable...
    }
}