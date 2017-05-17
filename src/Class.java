import static java.lang.System.out;

public final class Class {
    public static void main(String[] args) {
        String upper = upper("Mierzejewski, Krzysztof");
        out.println(upper + " = " + count(upper));
        if (1 == 2) {

        }
    }

    private static String upper(String s) {
        String aux = s.toUpperCase();
        return aux;
    }

    public static int count(String s) {
        int length = s.length();
        return length;
    }

    private static void log(String str, Object ob, Object o, int i, boolean b, double d, float f, byte by, short s, long l, char c) {
        out.print((char) 27 + "[32m");
        out.println(str);
        out.print((char) 27 + "[0m");
    }
}

