package org.jacoco.core.internal.diff;

import org.jacoco.core.utils.CollectionUtils;
import org.jacoco.core.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private static String[] notCheckPkgsOrFiles;

    public static String[] getNotCheckPkgsOrFiles() {
        return notCheckPkgsOrFiles;
    }

    public static void setNotCheckPkgsOrFiles(String[] pkgs) {
        notCheckPkgsOrFiles = pkgs;
    }

    public static List<String> getAllNotCheck() {
        List<String> list = new ArrayList<>();
        if (!CollectionUtils.isNullOrEmpty(notCheckPkgsOrFiles)) {
            for (String s : notCheckPkgsOrFiles) {
                if (s.toLowerCase().endsWith(".java") || s.toLowerCase().endsWith(".kt")) {
                    String tmp = s.substring(0, s.lastIndexOf("."));
                    String ext = s.substring(s.lastIndexOf("."));
                    list.add(tmp.replaceAll("\\.", "/") + ext);
                } else {
                    list.add(s.replaceAll("\\.", "/"));
                }
            }
        }
        LogUtils.logWithWrap(list.toString());
        return list;
    }
}
