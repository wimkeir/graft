public class Aliasing {

    public static void main(String[] args) {
        Obj x = new Obj();
        Obj y = x;
        int a = y.a;
        y.b = 2;
    }

    private static class Obj {
        public int a;
        public int b;
    }
}