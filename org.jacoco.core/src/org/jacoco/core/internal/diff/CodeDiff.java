/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.diff;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.alibaba.fastjson.JSON;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.StringUtils;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.utils.FileUtils;
import org.jacoco.core.utils.LogUtils;

/**
 * 代码版本比较
 */
public class CodeDiff {
    public final static String REF_HEADS = "refs/heads/";
    public final static String MASTER = "master";

    //  排除的路径
    private final static List<String> notCheckPath = new ArrayList<String>() {
        {
            //  排除测试类
            add("/src/test/java/");
        }
    };
    // 需要校验的文件后缀
    private final static List<String> checkedFileSuffix = new ArrayList<String>() {
        {
            add(".java");
            add(".kt");
        }
    };

    /**
     * 分支和分支之间的比较
     *
     * @param gitPath       git路径
     * @param newBranchName 新分支名称
     * @param oldBranchName 旧分支名称
     * @return
     */
    public static List<ClassInfo> diffBranchToBranch(String gitPath, String newBranchName, String oldBranchName) {
        return diffMethods(gitPath, newBranchName, oldBranchName);
    }

    private static List<ClassInfo> diffMethods(String gitPath, String newBranchName, String oldBranchName) {
        try {
            //  获取本地分支
            GitAdapter gitAdapter = new GitAdapter(gitPath);
            Git git = gitAdapter.getGit();
            Ref localBranchRef = gitAdapter.getRepository().exactRef(REF_HEADS + newBranchName);
            Ref localMasterRef = gitAdapter.getRepository().exactRef(REF_HEADS + oldBranchName);
            //  更新本地分支
            try {
                gitAdapter.checkOutAndPull(localMasterRef, oldBranchName);
                gitAdapter.checkOutAndPull(localBranchRef, newBranchName);
            } catch (GitAPIException e) {
                System.out.println("diffMethods 更新分支失败！！！");
                e.printStackTrace();
            }
            //  获取分支信息
            AbstractTreeIterator newTreeParser = gitAdapter.prepareTreeParser(localBranchRef);
            AbstractTreeIterator oldTreeParser = gitAdapter.prepareTreeParser(localMasterRef);
            //  对比差异
            List<DiffEntry> diffs = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).setShowNameAndStatusOnly(true).call();

            printDiffInfo(diffs, "diffMethods");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            //设置比较器为忽略空白字符对比（Ignores all whitespace）
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setRepository(git.getRepository());
            return batchPrepareDiffMethod(gitAdapter, newBranchName, oldBranchName, df, diffs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 单分支Tag版本之间的比较
     *
     * @param gitPath 本地git代码仓库路径
     * @param newTag  新Tag版本
     * @param oldTag  旧Tag版本
     * @return
     */
    public static List<ClassInfo> diffTagToTag(String gitPath, String branchName, String newTag, String oldTag) {
        if (StringUtils.isEmptyOrNull(gitPath) || StringUtils.isEmptyOrNull(branchName) || StringUtils.isEmptyOrNull(newTag) || StringUtils.isEmptyOrNull(oldTag)) {
            throw new IllegalArgumentException("Parameter(local gitPath,develop branchName,new Tag,old Tag) can't be empty or null !");
        } else if (newTag.equals(oldTag)) {
            throw new IllegalArgumentException("Parameter new Tag and old Tag can't be the same");
        }
        File gitPathDir = new File(gitPath);
        if (!gitPathDir.exists()) {
            throw new IllegalArgumentException("Parameter local gitPath is not exit !");
        }

        return diffTagMethods(gitPath, branchName, newTag, oldTag);
    }

    private static List<ClassInfo> diffTagMethods(String gitPath, String branchName, String newTag, String oldTag) {
        try {
            //  init local repository
            GitAdapter gitAdapter = new GitAdapter(gitPath);
            Git git = gitAdapter.getGit();
            Repository repo = gitAdapter.getRepository();
            Ref localBranchRef = repo.exactRef(REF_HEADS + branchName);

            //  update local repository
            try {
                gitAdapter.checkOutAndPull(localBranchRef, branchName);
            } catch (GitAPIException e) {
                System.out.println("！！！ diffTagMethods update local repository失败！！！");
                e.printStackTrace();
            }

            ObjectId head = repo.resolve(newTag + "^{tree}");
            ObjectId previousHead = repo.resolve(oldTag + "^{tree}");

            // Instanciate a reader to read the data from the Git database
            ObjectReader reader = repo.newObjectReader();
            // Create the tree iterator for each commit
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, previousHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);

            //  对比差异
            List<DiffEntry> diffs = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).setShowNameAndStatusOnly(true).call();

            printDiffInfo(diffs, "diffTagMethods");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            //设置比较器为忽略空白字符对比（Ignores all whitespace）
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setRepository(repo);
            return batchPrepareDiffMethodForTag(gitAdapter, newTag, oldTag, df, diffs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 多线程执行对比
     *
     * @return
     */
    private static List<ClassInfo> batchPrepareDiffMethodForTag(final GitAdapter gitAdapter, final String newTag, final String oldTag, final DiffFormatter df, List<DiffEntry> diffs) {
        int threadSize = 100;
        int dataSize = diffs.size();
        int threadNum = dataSize / threadSize + 1;
        boolean special = dataSize % threadSize == 0;
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        List<Callable<List<ClassInfo>>> tasks = new ArrayList<Callable<List<ClassInfo>>>();
        Callable<List<ClassInfo>> task;
        List<DiffEntry> cutList;
        //  分解每条线程的数据
        for (int i = 0; i < threadNum; i++) {
            if (i == threadNum - 1) {
                if (special) {
                    break;
                }
                cutList = diffs.subList(threadSize * i, dataSize);
            } else {
                cutList = diffs.subList(threadSize * i, threadSize * (i + 1));
            }
            final List<DiffEntry> diffEntryList = cutList;
            task = () -> {
                List<ClassInfo> allList = new ArrayList<>();
                for (DiffEntry diffEntry : diffEntryList) {
                    ClassInfo classInfo = prepareDiffMethodForTag(gitAdapter, newTag, oldTag, df, diffEntry);
                    if (classInfo != null) {
                        allList.add(classInfo);
                    }
                }
                return allList;
            };
            // 这里提交的任务容器列表和返回的Future列表存在顺序对应的关系
            tasks.add(task);
        }
        List<ClassInfo> allClassInfoList = new ArrayList<ClassInfo>();
        fillClassList(executorService, tasks, allClassInfoList);
        return allClassInfoList;
    }

    private static void fillClassList(ExecutorService executorService, List<Callable<List<ClassInfo>>> tasks, List<ClassInfo> allClassInfoList) {
        try {
            List<Future<List<ClassInfo>>> results = executorService.invokeAll(tasks);
            //结果汇总
            for (Future<List<ClassInfo>> future : results) {
                allClassInfoList.addAll(future.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            executorService.shutdown();
        }
    }

    /**
     * 单个差异文件对比
     *
     * @param gitAdapter
     * @param newTag
     * @param oldTag
     * @param df
     * @param diffEntry
     * @return
     */
    private synchronized static ClassInfo prepareDiffMethodForTag(GitAdapter gitAdapter, String newTag, String oldTag, DiffFormatter df, DiffEntry diffEntry) {
        List<MethodInfo> methodInfoList = new ArrayList<>();
        try {
            String newJavaPath = diffEntry.getNewPath();
            //  排除测试类
            if (!checkPath(newJavaPath)) {
                return null;
            }
            //  非java文件 和 删除类型不记录
            if (!checkFileSuffix(newJavaPath) || diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                return null;
            }
            ClassInfo classInfo = null;
            String newClassContent = gitAdapter.getTagRevisionSpecificFileContent(newTag, newJavaPath);
            ASTGenerator newAstGenerator = new ASTGenerator(newClassContent);
            /*  新增类型   */
            if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD) {
                classInfo = newAstGenerator.getClassInfo(newJavaPath);
            } else {
                /*  修改类型  */
                //  获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
                FileHeader fileHeader = df.toFileHeader(diffEntry);
                List<int[]> addLines = new ArrayList<>();
                List<int[]> delLines = new ArrayList<>();
                EditList editList = fileHeader.toEditList();
                fillEditLines(addLines, delLines, editList);
                String oldJavaPath = diffEntry.getOldPath();
                String oldClassContent = gitAdapter.getTagRevisionSpecificFileContent(oldTag, oldJavaPath);
                ASTGenerator oldAstGenerator = new ASTGenerator(oldClassContent);
                MethodDeclaration[] newMethods = newAstGenerator.getMethods();
                MethodDeclaration[] oldMethods = oldAstGenerator.getMethods();
                Map<String, MethodDeclaration> methodsMap = new HashMap<String, MethodDeclaration>();
                for (MethodDeclaration oldMethod : oldMethods) {
                    methodsMap.put(oldMethod.getName().toString() + oldMethod.parameters().toString(), oldMethod);
                }
                fillMethodInfos(methodInfoList, newAstGenerator, newMethods, methodsMap);
                classInfo = newAstGenerator.getClassInfo(methodInfoList, addLines, delLines, newJavaPath);
            }
            if (isKotlin(newJavaPath) && classInfo == null) {
                KotlinASTGenerator kGenerator = new KotlinASTGenerator(newClassContent, newJavaPath);
                ClassInfo kCi = kGenerator.getClassInfo(newJavaPath);
                CoverageBuilder.addSourceInfo(kCi);
            } else {
                CoverageBuilder.addSourceInfo(classInfo);
            }
            return classInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void fillEditLines(List<int[]> addLines, List<int[]> delLines, EditList editList) {
        for (Edit edit : editList) {
            if (edit.getLengthA() > 0) {
                delLines.add(new int[]{edit.getBeginA(), edit.getEndA()});
            }
            if (edit.getLengthB() > 0) {
                addLines.add(new int[]{edit.getBeginB(), edit.getEndB()});
            }
        }
    }

    /**
     * 多线程执行对比
     *
     * @return
     */
    private static List<ClassInfo> batchPrepareDiffMethod(final GitAdapter gitAdapter, final String branchName, final String oldBranchName, final DiffFormatter df, List<DiffEntry> diffs) {
        int threadSize = 100;
        int dataSize = diffs.size();
        int threadNum = dataSize / threadSize + 1;
        boolean special = dataSize % threadSize == 0;
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        List<Callable<List<ClassInfo>>> tasks = new ArrayList<Callable<List<ClassInfo>>>();
        Callable<List<ClassInfo>> task;
        List<DiffEntry> cutList;
        //  分解每条线程的数据
        for (int i = 0; i < threadNum; i++) {
            if (i == threadNum - 1) {
                if (special) {
                    break;
                }
                cutList = diffs.subList(threadSize * i, dataSize);
            } else {
                cutList = diffs.subList(threadSize * i, threadSize * (i + 1));
            }
            final List<DiffEntry> diffEntryList = cutList;
            task = () -> {
                List<ClassInfo> allList = new ArrayList<>();
                if (ReadDiffFromFile.useLocal()) {
                    // add by vita 测试代码，读取本地已有的diff信息
                    allList = ReadDiffFromFile.read();
                    LogUtils.log(">>>>>>> batchPrepareDiffMethod local data:: " + allList.size());
                } else {
                    // add by vita 将diff信息写入文件，方便查看
                    if (LogUtils.isLoggable()) {
                        FileUtils.writeFile("/Users/wangt/Workspace/Test/JacocoDemoReport/log/diff-origin.json", JSON.toJSONString(diffEntryList));
                    }

                    for (DiffEntry diffEntry : diffEntryList) {
//                        LogUtils.log(">>>>>>> batchPrepareDiffMethod diffEntry:: " + diffEntry);
                        ClassInfo classInfo = prepareDiffMethod(gitAdapter, branchName, oldBranchName, df, diffEntry);
                        if (classInfo != null) {
                            allList.add(classInfo);
                            LogUtils.log("CodeDiff", "batchPrepareDiffMethod", "** " + classInfo);
                        }
                    }
                    // add by vita 将diff信息写入文件，方便查看
                    if (LogUtils.isLoggable()) {
                        FileUtils.writeFile("/Users/wangt/Workspace/Test/JacocoDemoReport/log/diff-clsssinfo.json", JSON.toJSONString(allList));
                    }
                }
                return allList;
            };
            // 这里提交的任务容器列表和返回的Future列表存在顺序对应的关系
            tasks.add(task);
        }
        List<ClassInfo> allClassInfoList = new ArrayList<ClassInfo>();
        fillClassList(executorService, tasks, allClassInfoList);
        return allClassInfoList;
    }

    /**
     * 单个差异文件对比
     *
     * @param gitAdapter
     * @param branchName
     * @param oldBranchName
     * @param df
     * @param diffEntry
     * @return
     */
    private synchronized static ClassInfo prepareDiffMethod(GitAdapter gitAdapter, String branchName, String oldBranchName, DiffFormatter df, DiffEntry diffEntry) {
        List<MethodInfo> methodInfoList = new ArrayList<>();
        try {
            String newJavaPath = diffEntry.getNewPath();
            //  排除测试类
            if (!checkPath(newJavaPath)) {
                return null;
            }
            //  非java文件 和 删除类型不记录
            if (!checkFileSuffix(newJavaPath) || diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                return null;
            }
            ClassInfo classInfo = null;
            String newClassContent = gitAdapter.getBranchSpecificFileContent(branchName, newJavaPath);
            ASTGenerator newAstGenerator = new ASTGenerator(newClassContent);
            /*  新增类型   */
            if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD) {
                classInfo = newAstGenerator.getClassInfo(newJavaPath);
            } else {
                /*  修改类型  */
                //  获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
                FileHeader fileHeader = df.toFileHeader(diffEntry);
                List<int[]> addLines = new ArrayList<>();
                List<int[]> delLines = new ArrayList<>();
                EditList editList = fileHeader.toEditList();
                fillEditLines(addLines, delLines, editList);

                Map<String, MethodDeclaration> methodsMap = new HashMap<String, MethodDeclaration>();
                if (diffEntry.getChangeType() != DiffEntry.ChangeType.ADD) {
                    String oldJavaPath = diffEntry.getOldPath();
                    String oldClassContent = gitAdapter.getBranchSpecificFileContent(oldBranchName, oldJavaPath);
                    ASTGenerator oldAstGenerator = new ASTGenerator(oldClassContent);
                    MethodDeclaration[] oldMethods = oldAstGenerator.getMethods();
                    for (MethodDeclaration oldMethod : oldMethods) {
                        methodsMap.put(oldMethod.getName().toString() + oldMethod.parameters().toString(), oldMethod);
                    }
                }

                MethodDeclaration[] newMethods = newAstGenerator.getMethods();

                fillMethodInfos(methodInfoList, newAstGenerator, newMethods, methodsMap);
                classInfo = newAstGenerator.getClassInfo(methodInfoList, addLines, delLines, newJavaPath);
            }
            if (isKotlin(newJavaPath) && classInfo == null) {
                KotlinASTGenerator kGenerator = new KotlinASTGenerator(newClassContent, newJavaPath);
                ClassInfo kCi = kGenerator.getClassInfo(newJavaPath);
                CoverageBuilder.addSourceInfo(kCi);
            } else {
                CoverageBuilder.addSourceInfo(classInfo);
            }
            return classInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void fillMethodInfos(List<MethodInfo> methodInfoList, ASTGenerator newAstGenerator, MethodDeclaration[] newMethods, Map<String, MethodDeclaration> methodsMap) {
        for (final MethodDeclaration method : newMethods) {
            // 如果方法名是新增的,则直接将方法加入List
            if (!ASTGenerator.isMethodExist(method, methodsMap)) {
                MethodInfo methodInfo = newAstGenerator.getMethodInfo(method);
                methodInfoList.add(methodInfo);
                continue;
            }
            // 如果两个版本都有这个方法,则根据MD5判断方法是否一致
            if (!ASTGenerator.isMethodTheSame(method, methodsMap.get(method.getName().toString() + method.parameters().toString()))) {
                MethodInfo methodInfo = newAstGenerator.getMethodInfo(method);
                methodInfoList.add(methodInfo);
            }
        }
    }

    /**
     * 校验文件后缀名
     */
    private static boolean checkFileSuffix(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
//        LogUtils.log("CodeDiff checkFileSuffix: " + path);
        for (String suffix : checkedFileSuffix) {
            if (path.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
//        LogUtils.log("CodeDiff checkPath: " + path);
        for (String p : notCheckPath) {
            if (path.contains(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打印差异信息
     */
    private static void printDiffInfo(List<DiffEntry> diffs, String name) {
        if (!LogUtils.isLoggable()) {
            return;
        }
        LogUtils.log("\n");
        LogUtils.log(name + " 对比差异：" + diffs);
        if (diffs != null && diffs.size() > 0) {
            LogUtils.log("\n");
            LogUtils.log("======================= " + name + " 对比差异 文件列表 start =======================");
            for (DiffEntry diffE : diffs) {
                String newPath = diffE.getNewPath();
                if (checkFileSuffix(newPath)) {
                    LogUtils.log("[" + diffE.getChangeType() + "] " + newPath);
                }
            }
            LogUtils.log("======================= " + name + " 对比差异 文件列表 end =======================");
            LogUtils.log("\n");
        }
    }

    public static boolean isKotlin(String path) {
        return !org.jacoco.core.utils.StringUtils.isNullOrEmpty(path) && path.toLowerCase().endsWith(".kt");
    }

}
