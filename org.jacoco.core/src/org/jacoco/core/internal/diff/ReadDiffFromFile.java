package org.jacoco.core.internal.diff;

import com.alibaba.fastjson.JSON;
import org.eclipse.jgit.util.StringUtils;
import org.jacoco.core.utils.FileUtils;
import org.jacoco.core.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 从指定文件中读取 git diff 结果
 */
public class ReadDiffFromFile {

    private static String sFilePath;

    public static void setFilePath(String path) {
        sFilePath = path;
        LogUtils.log(">>>>>>> setFilePath:: " + sFilePath);
    }

    public static boolean useLocal() {
        return !StringUtils.isEmptyOrNull(sFilePath);
    }

    public static List<ClassInfo> read() {
        if (StringUtils.isEmptyOrNull(sFilePath)) {
            LogUtils.log("!!! sFilePath is null");
            return getDef();
        }
        LogUtils.log(">>>>>>> sFilePath = " + sFilePath);
        String content = FileUtils.readFile(sFilePath);
        if (StringUtils.isEmptyOrNull(content)) {
            LogUtils.log("!!! content from sFilePath is null");
            return getDef();
        }
        List<ClassInfo> infoList = new ArrayList<>();
        try {
            List<MyClassInfo> myInfoList = JSON.parseArray(content, MyClassInfo.class);
            for (MyClassInfo info : myInfoList) {
                ClassInfo classInfo = convertClassInfo(info);
                if (classInfo != null) {
                    infoList.add(classInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infoList;
    }

    private static ClassInfo convertClassInfo(MyClassInfo myClassInfo) {
        if (myClassInfo == null) {
            return null;
        }
        ClassInfo classInfo = new ClassInfo();
        classInfo.setType(myClassInfo.getType());
        classInfo.setClassFile(myClassInfo.getClassFile());
        classInfo.setClassName(myClassInfo.getClassName());
        classInfo.setPackages(myClassInfo.getPackages());
        classInfo.setMethodInfos(myClassInfo.methodInfoList);
        classInfo.setAddLines(myClassInfo.getAddLines());
        classInfo.setDelLines(myClassInfo.getDelLines());
        return classInfo;
    }

    private static List<ClassInfo> getDef() {
        List<ClassInfo> infoList = new ArrayList<>();

        ClassInfo app = new ClassInfo();
        app.setClassFile("app/src/main/java/com/jacoco/demo/App.java");
        app.setType(Type.MODIFY);
        app.setPackages("com.jacoco.demo");
        app.setClassName("App");
        List<MethodInfo> appMethodInfos = new ArrayList<>();
        MethodInfo appM1 = new MethodInfo();
        appM1.methodName = "onCreate";
        appM1.parameters = "";
        appMethodInfos.add(appM1);
        app.setMethodInfos(appMethodInfos);
        List<int[]> appAddLines = new ArrayList<>();
        appAddLines.add(new int[]{11, 12});
        appAddLines.add(new int[]{29, 30});
        app.setAddLines(appAddLines);
        infoList.add(app);

        ClassInfo mainAct = new ClassInfo();
        mainAct.setClassFile("app/src/main/java/com/jacoco/demo/MainActivity.java");
        mainAct.setType(Type.MODIFY);
        mainAct.setPackages("com.jacoco.demo");
        mainAct.setClassName("MainActivity");
        List<MethodInfo> mainActMethodInfos = new ArrayList<>();
        MethodInfo mainActM1 = new MethodInfo();
        mainActM1.methodName = "onCreate";
        mainActM1.parameters = "Bundle";
        mainActMethodInfos.add(mainActM1);
        MethodInfo mainActM2 = new MethodInfo();
        mainActM2.methodName = "addDynamicShortcuts";
        mainActM2.parameters = "";
        mainActMethodInfos.add(mainActM2);
        MethodInfo mainActM3 = new MethodInfo();
        mainActM3.methodName = "onClick";
        mainActM3.parameters = "View";
        mainActMethodInfos.add(mainActM3);
        mainAct.setMethodInfos(mainActMethodInfos);
        List<int[]> mainActAddLines = new ArrayList<>();
        mainActAddLines.add(new int[]{4, 6});
        mainActAddLines.add(new int[]{9, 9});
        mainActAddLines.add(new int[]{13, 18});
        mainActAddLines.add(new int[]{21, 22});
        mainActAddLines.add(new int[]{30, 33});
        mainActAddLines.add(new int[]{36, 60});
        mainActAddLines.add(new int[]{66, 77});
        mainActAddLines.add(new int[]{82, 82});
        mainAct.setAddLines(mainActAddLines);
        infoList.add(mainAct);

        ClassInfo monitorUtils = new ClassInfo();
        monitorUtils.setClassFile("app/src/main/java/com/jacoco/demo/blankmonitor/MonitorUtils.java");
        monitorUtils.setType(Type.ADD);
        monitorUtils.setPackages("com.jacoco.demo");
        monitorUtils.setClassName("MonitorUtils");
        infoList.add(monitorUtils);

        ClassInfo testBlankAct = new ClassInfo();
        testBlankAct.setClassFile("app/src/main/java/com/jacoco/demo/blankmonitor/TestBlankActivity.java");
        testBlankAct.setType(Type.ADD);
        testBlankAct.setPackages("com.jacoco.demo");
        testBlankAct.setClassName("TestBlankActivity");
        infoList.add(testBlankAct);

        ClassInfo testGuideAct = new ClassInfo();
        testGuideAct.setClassFile("app/src/main/java/com/jacoco/demo/guide/TestGuideActivity.java");
        testGuideAct.setType(Type.ADD);
        testGuideAct.setPackages("com.jacoco.demo");
        testGuideAct.setClassName("TestGuideActivity");
        infoList.add(testGuideAct);

        ClassInfo amyLauncherUtil = new ClassInfo();
        amyLauncherUtil.setClassFile("app/src/main/java/com/jacoco/demo/shortcut/AmyLauncherUtil.java");
        amyLauncherUtil.setType(Type.ADD);
        amyLauncherUtil.setPackages("com.jacoco.demo");
        amyLauncherUtil.setClassName("AmyLauncherUtil");
        infoList.add(amyLauncherUtil);

        ClassInfo amyShortcutUtil = new ClassInfo();
        amyShortcutUtil.setClassFile("app/src/main/java/com/jacoco/demo/shortcut/AmyShortcutUtil.java");
        amyShortcutUtil.setType(Type.ADD);
        amyShortcutUtil.setPackages("com.jacoco.demo");
        amyShortcutUtil.setClassName("AmyShortcutUtil");
        infoList.add(amyShortcutUtil);

        ClassInfo amyUtils = new ClassInfo();
        amyUtils.setClassFile("app/src/main/java/com/jacoco/demo/shortcut/AmyUtils.java");
        amyUtils.setType(Type.ADD);
        amyUtils.setPackages("com.jacoco.demo");
        amyUtils.setClassName("AmyUtils");
        infoList.add(amyUtils);

        ClassInfo checkShortCutUtil = new ClassInfo();
        checkShortCutUtil.setClassFile("app/src/main/java/com/jacoco/demo/shortcut/CheckShortCutUtil.java");
        checkShortCutUtil.setType(Type.ADD);
        checkShortCutUtil.setPackages("com.jacoco.demo");
        checkShortCutUtil.setClassName("CheckShortCutUtil");
        infoList.add(checkShortCutUtil);

        ClassInfo shortcutCallback = new ClassInfo();
        shortcutCallback.setClassFile("app/src/main/java/com/jacoco/demo/shortcut/ShortcutCallback.java");
        shortcutCallback.setType(Type.ADD);
        shortcutCallback.setPackages("com.jacoco.demo");
        shortcutCallback.setClassName("ShortcutCallback");
        infoList.add(shortcutCallback);

        ClassInfo shortcutPermissionChecker = new ClassInfo();
        shortcutPermissionChecker.setClassFile("app/src/main/java/com/jacoco/demo/shortcut/ShortcutPermissionChecker.java");
        shortcutPermissionChecker.setType(Type.ADD);
        shortcutPermissionChecker.setPackages("com.jacoco.demo");
        shortcutPermissionChecker.setClassName("ShortcutPermissionChecker");
        infoList.add(shortcutPermissionChecker);

        ClassInfo testShortcutAct = new ClassInfo();
        testShortcutAct.setClassFile("app/src/main/java/com/jacoco/demo/shortcut/TestShortcutActivity.java");
        testShortcutAct.setType(Type.ADD);
        testShortcutAct.setPackages("com.jacoco.demo");
        testShortcutAct.setClassName("TestShortcutActivity");
        infoList.add(testShortcutAct);

        ClassInfo testSmartCropperAct = new ClassInfo();
        testSmartCropperAct.setClassFile("app/src/main/java/com/jacoco/demo/smartcropper/TestSmartCropperActivity.java");
        testSmartCropperAct.setType(Type.ADD);
        testSmartCropperAct.setPackages("com.jacoco.demo");
        testSmartCropperAct.setClassName("TestSmartCropperActivity");
        infoList.add(testSmartCropperAct);

        ClassInfo nodePickViewJava = new ClassInfo();
        nodePickViewJava.setClassFile("app/src/main/java/com/jacoco/demo/ui/NodePickViewJava.java");
        nodePickViewJava.setType(Type.ADD);
        nodePickViewJava.setPackages("com.jacoco.demo");
        nodePickViewJava.setClassName("NodePickViewJava");
        infoList.add(nodePickViewJava);

        ClassInfo pointProcessBar = new ClassInfo();
        pointProcessBar.setClassFile("app/src/main/java/com/jacoco/demo/ui/PointProcessBar.java");
        pointProcessBar.setType(Type.ADD);
        pointProcessBar.setPackages("com.jacoco.demo");
        pointProcessBar.setClassName("PointProcessBar");
        infoList.add(pointProcessBar);

        ClassInfo shadowDrawable = new ClassInfo();
        shadowDrawable.setClassFile("app/src/main/java/com/jacoco/demo/ui/ShadowDrawable.java");
        shadowDrawable.setType(Type.ADD);
        shadowDrawable.setPackages("com.jacoco.demo");
        shadowDrawable.setClassName("ShadowDrawable");
        infoList.add(shadowDrawable);

        ClassInfo testUiAct = new ClassInfo();
        testUiAct.setClassFile("app/src/main/java/com/jacoco/demo/ui/TestUiActivity.java");
        testUiAct.setType(Type.ADD);
        testUiAct.setPackages("com.jacoco.demo");
        testUiAct.setClassName("TestUiActivity");
        infoList.add(testUiAct);

        ClassInfo viewUtil = new ClassInfo();
        viewUtil.setClassFile("app/src/main/java/com/jacoco/demo/ui/ViewUtil.java");
        viewUtil.setType(Type.ADD);
        viewUtil.setPackages("com.jacoco.demo");
        viewUtil.setClassName("ViewUtil");
        infoList.add(viewUtil);

        ClassInfo callback = new ClassInfo();
        callback.setClassFile("app/src/main/java/com/jacoco/demo/utils/Callback.java");
        callback.setType(Type.ADD);
        callback.setPackages("com.jacoco.demo");
        callback.setClassName("Callback");
        infoList.add(callback);

        ClassInfo displayUtil = new ClassInfo();
        displayUtil.setClassFile("app/src/main/java/com/jacoco/demo/utils/DisplayUtil.java");
        displayUtil.setType(Type.ADD);
        displayUtil.setPackages("com.jacoco.demo");
        displayUtil.setClassName("DisplayUtil");
        infoList.add(displayUtil);

        ClassInfo drawableUtils = new ClassInfo();
        drawableUtils.setClassFile("app/src/main/java/com/jacoco/demo/utils/DrawableUtils.java");
        drawableUtils.setType(Type.ADD);
        drawableUtils.setPackages("com.jacoco.demo");
        drawableUtils.setClassName("DrawableUtils");
        infoList.add(drawableUtils);

        ClassInfo fileUtils = new ClassInfo();
        fileUtils.setClassFile("app/src/main/java/com/jacoco/demo/utils/FileUtils.java");
        fileUtils.setType(Type.ADD);
        fileUtils.setPackages("com.jacoco.demo");
        fileUtils.setClassName("FileUtils");
        infoList.add(fileUtils);

        ClassInfo imageLoadUtil = new ClassInfo();
        imageLoadUtil.setClassFile("app/src/main/java/com/jacoco/demo/utils/ImageLoadUtil.java");
        imageLoadUtil.setType(Type.ADD);
        imageLoadUtil.setPackages("com.jacoco.demo");
        imageLoadUtil.setClassName("ImageLoadUtil");
        infoList.add(imageLoadUtil);

        ClassInfo imageLoaderListener = new ClassInfo();
        imageLoaderListener.setClassFile("app/src/main/java/com/jacoco/demo/utils/ImageLoaderListener.java");
        imageLoaderListener.setType(Type.ADD);
        imageLoaderListener.setPackages("com.jacoco.demo");
        imageLoaderListener.setClassName("ImageLoaderListener");
        infoList.add(imageLoaderListener);

        ClassInfo permissionUtil = new ClassInfo();
        permissionUtil.setClassFile("app/src/main/java/com/jacoco/demo/utils/PermissionUtil.java");
        permissionUtil.setType(Type.ADD);
        permissionUtil.setPackages("com.jacoco.demo");
        permissionUtil.setClassName("PermissionUtil");
        infoList.add(permissionUtil);

        ClassInfo resUtil = new ClassInfo();
        resUtil.setClassFile("app/src/main/java/com/jacoco/demo/utils/ResUtil.java");
        resUtil.setType(Type.ADD);
        resUtil.setPackages("com.jacoco.demo");
        resUtil.setClassName("ResUtil");
        infoList.add(resUtil);

        ClassInfo shareFileUtil = new ClassInfo();
        shareFileUtil.setClassFile("app/src/main/java/com/jacoco/demo/utils/ShareFileUtil.java");
        shareFileUtil.setType(Type.ADD);
        shareFileUtil.setPackages("com.jacoco.demo");
        shareFileUtil.setClassName("ShareFileUtil");
        infoList.add(shareFileUtil);

        ClassInfo numberProgressBar = new ClassInfo();
        numberProgressBar.setClassFile("libnumberprogressbar/src/main/java/com/daimajia/numberprogressbar/NumberProgressBar.java");
        numberProgressBar.setType(Type.ADD);
        numberProgressBar.setPackages("com.daimajia.numberprogressbar");
        numberProgressBar.setClassName("NumberProgressBar");
        infoList.add(numberProgressBar);

        ClassInfo onProgressBarListener = new ClassInfo();
        onProgressBarListener.setClassFile("libnumberprogressbar/src/main/java/com/daimajia/numberprogressbar/OnProgressBarListener.java");
        onProgressBarListener.setType(Type.ADD);
        onProgressBarListener.setPackages("com.daimajia.numberprogressbar");
        onProgressBarListener.setClassName("OnProgressBarListener");
        infoList.add(onProgressBarListener);

        return infoList;
    }

    public static class MyClassInfo extends ClassInfo {
        public List<MethodInfo> methodInfoList;
    }
}
