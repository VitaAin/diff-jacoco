package org.jacoco.core.utils;

public class StringUtils {

    public static boolean equals(CharSequence c1, CharSequence c2) {
        return (c1 == null && c2 == null)
                || (c1 != null && c2 != null && c1.equals(c2));
    }

    public static boolean equalsIgnoreCase(String c1, String c2) {
        return (c1 == null && c2 == null)
                || (c1 != null && c2 != null && c1.equalsIgnoreCase(c2));
    }

    public static boolean isNullOrEmpty(CharSequence c) {
        return c == null || c.length() == 0;
    }

    public static boolean isNullOrEmpty2(String c) {
        return c == null || c.trim().length() == 0;
    }

    public static String checkString(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

}
