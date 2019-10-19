public class Other {

    public static String someMethod(int a) {
        if (a > 5) {
            return "how  ";
        } else {
            return "now";
        }
    }

    public static void someOtherMethod(int c) {
        System.out.println(c-1);
    }

    private static class InnerClass {

        public int some;
        public int other;
        public int fields;
        public int here;

    }
}
