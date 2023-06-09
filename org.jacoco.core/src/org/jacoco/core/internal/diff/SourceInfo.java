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

import java.util.List;

public class SourceInfo {
    /**
     * java文件
     */
    private String classFile;
    /**
     * 包名
     */
    private String packages;

    /**
     * 新增的行数
     */
    private List<int[]> addLines;

    /**
     * 删除的行数
     */
    private List<int[]> delLines;

    /**
     * 修改类型
     */
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<int[]> getAddLines() {
        return addLines;
    }

    public void setAddLines(List<int[]> addLines) {
        this.addLines = addLines;
    }

    public List<int[]> getDelLines() {
        return delLines;
    }

    public void setDelLines(List<int[]> delLines) {
        this.delLines = delLines;
    }

    public String getClassFile() {
        return classFile;
    }

    public void setClassFile(String classFile) {
        this.classFile = classFile;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getSimpleInfo() {
        StringBuilder sb = new StringBuilder();

        sb.append("{")
                .append("type: ").append(type)
                .append(", ")
                .append("packages: ").append(packages)
                .append(", ")
                .append("classFile: ").append(classFile)
                .append(", ");
        sb.append("addLines: [");
        if (addLines != null && addLines.size() > 0) {
            for (int[] addL : addLines) {
                sb.append("[");
                for (int l : addL) {
                    sb.append(l)
                            .append(",");
                }
                sb.append("],");
            }
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSimpleInfo();
    }
}
