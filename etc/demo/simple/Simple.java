public class Simple {

    private static int attr;

    public static void main(String[] args) {

//        int[] a = new int[5];
//        int c = a[source() * 2];
//
        int a = source();
        if (Math.random() > 1) {
            sanitizer(a);
        } else {
            // comment out this line so tainted variable a propagates to sink
//            sanitizer(attr);
        }
        sink(a - 1);

        for (int i = 1; i < source(); i++) {
            int b = i + 2;
        }

        for (int i = 1; i < source(); i++) {
            int b = i - 2;
        }
    }

    public static int source() {
        return (int) Math.random() + 2  ;
    }

    public static void sanitizer(int a) {
        assert a > 2;
    }

    public static void sink(int a) {
        assert a < 1;
    }

}
