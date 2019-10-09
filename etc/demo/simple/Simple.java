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
        sink(a - 1);
    }

    public static int source() {
        return (int) Math.random();
    }

    public static void sanitizer(int a) {
        assert a > 2;
    }

    public static void sink(int a) {
        assert a < 1;
    }

}