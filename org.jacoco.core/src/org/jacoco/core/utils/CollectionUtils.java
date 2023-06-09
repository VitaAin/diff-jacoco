package org.jacoco.core.utils;

import java.util.Collection;
import java.util.Map;

public class CollectionUtils {

    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNullOrEmpty(Map<?, ?> c) {
        return c == null || c.isEmpty();
    }

}
