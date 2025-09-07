package utils;

public class Utils {
    public static String add(int i1, int i2) {
        return StringUtils.concat(
                "result: ",
                String.valueOf(MathUtils.add(i1, i2))
        );
    }
}

class StringUtils {
    static String concat(String s1, String s2) {
        return s1 + s2;
    }
}

class MathUtils {
    static int add(int i1, int i2) {
        return i1 + i2;
    }
    static int add2(int i1, int i2) {
        return i1 + i2;
    }
}

