package org.jacoco.core.utils;

public class ConvertUtils {

    public static boolean convert2Bool(String value) {
        return convert2Bool(value, false);
    }

    public static boolean convert2Bool(String value, boolean defValue) {
        if (value == null || value.isEmpty()) {
            return defValue;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception ignore) {
        }
        return defValue;
    }

}
