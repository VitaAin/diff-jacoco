package test;

import org.jacoco.core.internal.diff.Config;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.utils.LogUtils;
import org.jacoco.startup.ReportGenerator;

import java.io.File;
import java.io.IOException;

public class ReportGeneratorTest {

    private static final String GIT_WORK_DIR = "/Users/wangt/Workspace/Test/JacocoDemo";
    private static final String CURRENT_BRANCH = "dev_3";
    private static final String COMPARED_BRANCH = "dev";
    private static final String TAG = "dev_3";
    private static final String COMPARED_TAG = "dev";
    private static final String GIT_USER_NAME = "wangting";
    private static final String GIT_USER_PWD = "WANGting215930";
    private static final String REPORT_DIR = "/Users/wangt/Workspace/JacocoReport/JacocoDemo/report";
    private static final String EXEC_PATH = "/Users/wangt/Workspace/JacocoReport/JacocoDemo/coverage.ec";
    private static final String[] EXEC_PATHS = {"/Users/wangt/Workspace/JacocoReport/JacocoDemo/coverage1.ec",
            "/Users/wangt/Workspace/JacocoReport/JacocoDemo/coverage2.ec"};
    private static final String[] PROJECT_SOURCE = {"/Users/wangt/Workspace/Test/JacocoDemo/app/src/main/java",
            "/Users/wangt/Workspace/Test/JacocoDemo/libnumberprogressbar/src/main/java"};
    private static final String[] PROJECT_CLASS = {"/Users/wangt/Workspace/Test/JacocoDemo/app/build/intermediates/javac/debug/classes",
            "/Users/wangt/Workspace/Test/JacocoDemo/app/build/tmp/kotlin-classes/debug",
            "/Users/wangt/Workspace/Test/JacocoDemo/libnumberprogressbar/build/intermediates/javac/debug/classes",
            "/Users/wangt/Workspace/Test/JacocoDemo/libnumberprogressbar/build/tmp/kotlin-classes/debug"};

    private static final String[] NOT_CHECK = {"com.jacoco.demo.jacoco",
        "com.jacoco.demo.App.java"};

    public static void main(String[] args) {
        String title = new File(GIT_WORK_DIR).getName();

        File[] sourceDirFiles = new File[PROJECT_SOURCE.length];
        for (int i = 0; i < PROJECT_SOURCE.length; ++i) {
            sourceDirFiles[i] = new File(PROJECT_SOURCE[i]);
        }
        File[] classDirFiles = new File[PROJECT_CLASS.length];
        for (int i = 0; i < PROJECT_CLASS.length; ++i) {
            classDirFiles[i] = new File(PROJECT_CLASS[i]);
        }

        File[] execFiles = new File[EXEC_PATHS.length];
        for (int i = 0; i < execFiles.length; ++i) {
            execFiles[i] = new File(EXEC_PATHS[i]);
        }

        try {
            LogUtils.setLoggable(true);
            GitAdapter.setUserInfo(GIT_USER_NAME, GIT_USER_PWD);
            Config.setNotCheckPkgsOrFiles(NOT_CHECK);
            ReportGenerator.create(title,
//                    new File(EXEC_PATH),
                    execFiles,
                    new File(REPORT_DIR),
                    sourceDirFiles,
                    classDirFiles,
                    GIT_WORK_DIR,
                    CURRENT_BRANCH,
                    COMPARED_BRANCH,
                    null,
                    null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
