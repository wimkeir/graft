public class Simple {

    private static int attr;

    public static void main(String[] args) {

        int a = source();
        if (Math.random() > 1) {
            sanitizer(a);
        } else {
            // comment out this line so tainted variable a propagates to sink
            sanitizer(attr);
        }
        sink(a);
    }

    public static int source() {
        return (int) Math.random();
    }

    public static void sanitizer(int a) {
        // sanitize args...
    }

    public static void sink(int a) {
        // sensitive sink...
    }

}