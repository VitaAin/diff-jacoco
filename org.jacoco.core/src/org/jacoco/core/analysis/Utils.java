package org.jacoco.core.analysis;

import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.SourceInfo;
import org.jacoco.core.internal.diff.Type;
import org.jacoco.core.utils.CollectionUtils;
import org.jacoco.core.utils.LogUtils;
import org.jacoco.core.utils.StringUtils;

import java.util.List;

public class Utils {

    public static boolean linesContainsAddLines(int lineStart, int lineEnd, List<int[]> addLines, String type) {
        if (StringUtils.equalsIgnoreCase(Type.ADD, type)) {
            return true;
        }
        if (CollectionUtils.isNullOrEmpty(addLines)) {
            return false;
        }
        for (int[] ints : addLines) {
            if (CollectionUtils.isNullOrEmpty(ints)) {
                continue;
            }
            int addLineStart = ints[0] + 1;
            int addLineEnd = ints[1];
            if ((lineStart <= addLineStart && lineEnd >= addLineEnd)
                    || (lineStart <= addLineStart && lineEnd >= addLineStart && lineEnd <= addLineEnd)
                    || (lineStart >= addLineStart && lineStart <= addLineEnd && lineEnd >= addLineEnd)
                    || (lineStart >= addLineStart && lineStart <= addLineEnd && lineEnd >= addLineStart && lineEnd <= addLineEnd)) {
                return true;
            }
        }
        return false;
    }

    public static boolean lineInAddLines(int lineNr, List<int[]> addLines, String type) {
        if (StringUtils.equalsIgnoreCase(Type.ADD, type)) {
            return true;
        }
        if (CollectionUtils.isNullOrEmpty(addLines)) {
            return false;
        }
        for (int[] ints : addLines) {
            if (CollectionUtils.isNullOrEmpty(ints)) {
                continue;
            }
            if (ints[0] < lineNr && lineNr <= ints[1]) {
                return true;
            }
        }
        return false;
    }

    public static boolean linesContainsAddLines(int lineStart, int lineEnd, SourceInfo info) {
        if (info == null) {
            return false;
        }
        return linesContainsAddLines(lineStart, lineEnd, info.getAddLines(), info.getType());
    }

    public static boolean lineInAddLines(int lineNr, SourceInfo info) {
        if (info == null) {
            return false;
        }
        return lineInAddLines(lineNr, info.getAddLines(), info.getType());
    }

    public static boolean linesContainsAddLines(int lineStart, int lineEnd, ClassInfo classInfo) {
        if (classInfo == null) {
            return false;
        }
        return linesContainsAddLines(lineStart, lineEnd, classInfo.getAddLines(), classInfo.getType());
    }

    public static boolean lineInAddLines(int lineNr, ClassInfo classInfo) {
        if (classInfo == null) {
            return false;
        }
        return lineInAddLines(lineNr, classInfo.getAddLines(), classInfo.getType());
    }

    public static SourceInfo getSourceInfo(IClassCoverage c) {
        if (c == null) {
            return null;
        }
        return getSourceInfo(c.getPackageName() + "/" + c.getSourceFileName());
    }

    public static SourceInfo getSourceInfo(String path) {
        if (CollectionUtils.isNullOrEmpty(CoverageBuilder.sourceInfoMap)) {
            return null;
        }
        return CoverageBuilder.sourceInfoMap.get(path);
    }

    /**
     * @param className eg. com/jacoco/demo/ui/TestUiActivity$SeeMoreAdapter$1
     * @return
     */
    public static ClassInfo getClassInfoByClassName(String className) {
        if (CollectionUtils.isNullOrEmpty(CoverageBuilder.classInfos) || StringUtils.isNullOrEmpty(className)) {
            return null;
        }
        return CoverageBuilder.classInfos.get(className.split("\\$")[0].replaceAll("/", "."));
    }

    public static ClassInfo getClassInfo(String pkgName, String className) {
        if (CollectionUtils.isNullOrEmpty(CoverageBuilder.classInfos)) {
            return null;
        }
        return CoverageBuilder.classInfos.get(pkgName + "." + className);
    }

    public static ClassInfo getClassInfo(ISourceFileCoverage coverage) {
        if (coverage == null || CollectionUtils.isNullOrEmpty(CoverageBuilder.classInfos)) {
            return null;
        }
        String path = getPath(coverage.getPackageName(), coverage.getName());
        return getClassInfo(path);
    }

    public static ClassInfo getClassInfo(IClassCoverage coverage) {
        if (coverage == null || CollectionUtils.isNullOrEmpty(CoverageBuilder.classInfos)) {
            return null;
        }
        if (StringUtils.equals("MainActivity.java", coverage.getSourceFileName())) {
            LogUtils.logWrap();
        }
        String path = getPath(coverage.getPackageName(), coverage.getSourceFileName());
//        LogUtils.log("Utils", "getClassInfo", "path >> " + path + ", " + coverage.getPackageName() + ", " + coverage.getName() + ", " + coverage.getSourceFileName());
        ClassInfo classInfo = getClassInfo(path);
//        // 兼容 .kt 中无类名情况
//        if (classInfo == null) {
//            if (coverage.getSourceFileName().endsWith(".kt")) {
//                return getClassInfo(getPath(coverage.getPackageName(), coverage.getSourceFileName()) + "Kt");
//            }
//        }
        return classInfo;
    }

    public static ClassInfo getClassInfo(String srcFilePath) {
        if (StringUtils.isNullOrEmpty(srcFilePath) || CollectionUtils.isNullOrEmpty(CoverageBuilder.classInfos)) {
            return null;
        }
        return CoverageBuilder.classInfos.get(srcFilePath);
    }

    public static ClassInfo getClassInfoBySrcFilePath(String srcFilePath) {
        if (StringUtils.isNullOrEmpty(srcFilePath) || CollectionUtils.isNullOrEmpty(CoverageBuilder.classInfos)) {
            return null;
        }
        return CoverageBuilder.classInfos.get(getPath(srcFilePath));
    }

    private static String getPath(String pkg, String name) {
        String path = pkg + "/" + name;
        path = getPath(path);
        return path;
    }

    private static String getPath(String path) {
        try {
            if (path.contains(".")) {
                path = path.substring(0, path.lastIndexOf("."));
            }
            path = path.replaceAll("/", ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

}
