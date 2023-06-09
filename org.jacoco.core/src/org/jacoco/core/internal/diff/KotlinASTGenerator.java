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

import kastree.ast.Node;
import kastree.ast.psi.Parser;
import org.jacoco.core.utils.CollectionUtils;
import org.jacoco.core.utils.LogUtils;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KotlinASTGenerator {
    private Node.File file;
    private String filePath;
    public static final Parser parser = new Parser();

    public KotlinASTGenerator(String kotlinText, String filePath) {
        this.filePath = filePath;
        try {
            file = parser.parseFile(kotlinText, false);
        } catch (Exception e) {
            LogUtils.logWrap();
            LogUtils.log(filePath);
            e.printStackTrace();
        }
    }


    /**
     * 获取kotlin类包名
     *
     * @return
     */
    public String getPackageName() {
        if (file == null) {
            return "";
        }
        StringBuilder convertedListStr = new StringBuilder();
        int index = 0;
        for (String pkg : file.getPkg().getNames()) {
            index++;
            if (index < file.getPkg().getNames().size()) {
                convertedListStr.append(pkg).append(".");
            } else {
                convertedListStr.append(pkg);
            }

        }
        return convertedListStr.toString();
    }

    /**
     * 获取普通类单元
     *
     * @return
     */
    public String getJavaClass() {
        if (file == null) {
            return null;
        }
        if (file.getDecls().size() > 0) {
            if (file.getDecls().get(0).getClass().toString().equals("class kastree.ast.Node$Decl$Structured")) {
                return ((Node.Decl.Structured) file.getDecls().get(0)).getName();
            } else {
                // 这里可能全部都是方法，没有定义类的概念，所以要处理下
                return (filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".")));
            }

        } else {
            return null;
        }
    }

    /**
     * 获取kotlin类中所有方法
     *
     * @return 类中所有方法
     */
    public List<Node.Decl.Func> getAllMethods() {
        List<Node.Decl.Func> funcs = new ArrayList<>();
        for (Node.Decl decl : file.getDecls()) {
            if (decl.getClass().toString().equals("class kastree.ast.Node$Decl$Structured")) {
                for (Node.Decl decl1 : ((Node.Decl.Structured) decl).getMembers()) {
                    if (decl1.getClass().toString().equals("class kastree.ast.Node$Decl$Func")) {
                        funcs.add((Node.Decl.Func) decl1);
                    } else if (decl1.getClass().toString().equals("class kastree.ast.Node$Decl$Structured")) {
                        for (Node.Decl decl2 : ((Node.Decl.Structured) decl1).getMembers()) {
                            if (decl2.getClass().toString().equals("class kastree.ast.Node$Decl$Func")) {
                                funcs.add((Node.Decl.Func) decl2);
                            }
                        }
                    }
                }
            } else if (decl.getClass().toString().equals("class kastree.ast.Node$Decl$Func")) {
                funcs.add((Node.Decl.Func) decl);
            } else {
                System.out.println(decl.getClass().toString());
            }
        }
        return funcs;
    }

    public List<Node.Decl.Func> getMethods(List<Node.Decl> list) {
        if (CollectionUtils.isNullOrEmpty(list)) {
            return null;
        }
        List<Node.Decl.Func> funcs = new ArrayList<>();
        for (Node.Decl decl : list) {
            if (decl instanceof Node.Decl.Func) {
                funcs.add((Node.Decl.Func) decl);
            } else if (decl instanceof Node.Decl.Property){
                Node.Expr expr = ((Node.Decl.Property) decl).getExpr();
                if (expr instanceof Node.Expr.Object) {
                    List<Node.Decl> members = ((Node.Expr.Object) expr).getMembers();
                    for (Node.Decl m : members) {
                        if (m instanceof Node.Decl.Func) {
                            funcs.add((Node.Decl.Func) m);
                        }
                    }
                }
            } else {
                System.out.println(decl.getClass().toString());
            }
        }
        return funcs;
    }


    /**
     * 获取修改类型的类的信息以及其中的所有方法，排除接口类
     *
     * @param methodInfos
     * @param addLines
     * @param delLines
     * @return
     */
    public ClassInfo getClassInfo(List<MethodInfo> methodInfos, List<int[]> addLines, List<int[]> delLines, String filePath) {
        if (getJavaClass() == null) {
            return null;
        }
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(getJavaClass());
        classInfo.setPackages(getPackageName());
        classInfo.setMethodInfos(methodInfos);
        classInfo.setAddLines(addLines);
        classInfo.setDelLines(delLines);
        classInfo.setType(Type.MODIFY);
//        classInfo.setType("REPLACE");
        classInfo.setClassFile(filePath);
        return classInfo;
    }

    public ClassInfo getClassInfo(String filePath) {
        return getClassInfo(filePath, null, null);
    }

    /**
     * 获取新增类型的类的信息以及其中的所有方法，排除接口类
     *
     * @return
     */
    public ClassInfo getClassInfo(String filePath, List<int[]> addLines, List<int[]> delLines) {
        if (getJavaClass() == null) {
            return null;
        }
        List<Node.Decl.Func> methodDeclarations = getAllMethods();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(getJavaClass());
        classInfo.setPackages(getPackageName());
        classInfo.setType(Type.ADD);
        classInfo.setAddLines(addLines);
        classInfo.setDelLines(delLines);
        classInfo.setClassFile(filePath);
//        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
//        for (Node.Decl.Func method : methodDeclarations) {
//            MethodInfo methodInfo = new MethodInfo();
//            setMethodInfo(methodInfo, method);
//            methodInfoList.add(methodInfo);
//        }
//        classInfo.setMethodInfos(methodInfoList);
        return classInfo;
    }

    /**
     * 获取修改中的方法
     *
     * @param methodDeclaration
     * @return
     */
    public MethodInfo getMethodInfo(Node.Decl.Func methodDeclaration) {
        MethodInfo methodInfo = new MethodInfo();
        setMethodInfo(methodInfo, methodDeclaration);
        return methodInfo;
    }

    private void setMethodInfo(MethodInfo methodInfo, Node.Decl.Func methodDeclaration) {
        methodInfo.setMd5(methodDeclaration.getBody() == null ? "" : MD5Encode(methodDeclaration.getBody().toString()));
        methodInfo.setMethodName(methodDeclaration.getName());
        methodInfo.setParameters(methodDeclaration.getParams().toString());
        for (Object obj : methodDeclaration.getParams()) {
            methodInfo.addParameter(obj.toString());
        }
    }


    /**
     * 计算方法的MD5的值
     *
     * @param s
     * @return
     */
    public static String MD5Encode(String s) {
        String MD5String = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64en = new BASE64Encoder();
            MD5String = base64en.encode(md5.digest(s.getBytes("utf-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return MD5String;
    }

    /**
     * 判断方法是否存在
     *
     * @param method     新分支的方法
     * @param methodsMap master分支的方法
     * @return
     */
    public static boolean isMethodExist(final Node.Decl.Func method, final Map<String, Node.Decl.Func> methodsMap) {
        // 方法名+参数一致才一致
        if (!methodsMap.containsKey(method.getName() + method.getParams().toString() + (method.getReceiverType() == null ? "" : method.getReceiverType().toString()))) {
            return false;
        }
        return true;
    }

    /**
     * 判断方法是否一致
     *
     * @param method1
     * @param method2
     * @return
     */
    public static boolean isMethodTheSame(final Node.Decl.Func method1, final Node.Decl.Func method2) {
        if (method1.getBody() == null || method2.getBody() == null) {
            return true;
        } else if (method1.getBody().toString().equals(method2.getBody().toString())) {
            return true;
        }
        return false;
    }
}
