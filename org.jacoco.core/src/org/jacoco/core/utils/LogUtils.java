package org.jacoco.core.utils;

public class LogUtils {

    private static boolean sLoggable = false;

    public static void setLoggable(boolean loggable) {
        sLoggable = loggable;
    }

    public static boolean isLoggable() {
        return sLoggable;
    }

    public static void printStackTrace() {
        log(getStackTrace());
    }

    public static String getStackTrace() {
        Throwable throwable = new Throwable();
        StackTraceElement[] stackElements = throwable.getStackTrace();
        StringBuilder sb = new StringBuilder();
        if (null != stackElements) {
            for (int i = 0; i < stackElements.length; i++) {
                sb.append(stackElements[i].getClassName());
                sb.append(".").append(stackElements[i].getMethodName());
                sb.append("(").append(stackElements[i].getFileName()).append(":");
                sb.append(stackElements[i].getLineNumber()).append(")\n");
            }
        }
        return sb.toString();
    }

    public static void logWrap() {
        log("\n");
    }

    public static void logDoubleLine() {
        logDivider("=");
    }

    public static void logLine() {
        logDivider("-");
    }

    public static void logWithWrap(String log) {
        logWrap();
        log(log);
        logWrap();
    }

    public static void logWithLine(String log) {
        logWrap();
        logLine();
        log(log);
        logLine();
        logWrap();
    }

    public static void logWithDoubleLine(String log) {
        logWrap();
        logDoubleLine();
        log(log);
        logDoubleLine();
        logWrap();
    }

    public static void logWithSpecialTag(String log, String tag) {
        logWrap();
        logDivider(tag);
        log(log);
        logDivider(tag);
        logWrap();
    }

    public static void logDivider(String tag) {
        logDivider(tag, "");
    }

    public static void logDivider(String tag, String centerText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            sb.append(tag);
        }
        if (!StringUtils.isNullOrEmpty(centerText)) {
            sb.append("  ").append(centerText).append("  ");
        }
        for (int i = 0; i < 25; i++) {
            sb.append(tag);
        }
        log(sb.toString());
    }

    public static void log(String log) {
        if (!sLoggable) {
            return;
        }
        System.out.println(log);
    }

    public static void log(Class clz, String method, String log) {
        if (clz != null) {
            log(clz.getSimpleName(), method, log);
        } else {
            log("", method, log);
        }
    }

    public static void log(String className, String method, String... logs) {
        if ((logs == null || logs.length == 0) && (className == null || className.isEmpty()) && (method == null || method.isEmpty())) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (logs != null) {
            for (String s : logs) {
                sb.append(", ").append(s);
            }
        }
        String log = sb.toString();
        if (method != null && !method.isEmpty()) {
            log = method + ":: " + log;
        }
        if (className != null && !className.isEmpty()) {
            log = className + "  " + log;
        }
        log(log);
    }

}
