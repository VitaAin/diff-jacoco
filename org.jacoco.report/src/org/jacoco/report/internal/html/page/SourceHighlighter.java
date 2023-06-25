/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.html.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import org.jacoco.core.analysis.*;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.core.internal.diff.ClassInfo;
import org.jacoco.core.internal.diff.MethodInfo;
import org.jacoco.core.internal.diff.SourceInfo;
import org.jacoco.core.internal.diff.Type;
import org.jacoco.core.utils.LogUtils;
import org.jacoco.core.utils.StringUtils;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Creates a highlighted output of a source file.
 */
final class SourceHighlighter {

    private final Locale locale;

    private String lang;

    /**
     * Creates a new highlighter with default settings.
     *
     * @param locale locale for tooltip rendering
     */
    public SourceHighlighter(final Locale locale) {
        this.locale = locale;
        lang = "java";
    }

    /**
     * Specifies the source language. This value might be used for syntax
     * highlighting. Default is "java".
     *
     * @param lang source language identifier
     */
    public void setLanguage(final String lang) {
        this.lang = lang;
    }

    /**
     * Highlights the given source file.
     *
     * @param parent   parent HTML element
     * @param source   highlighting information
     * @param contents contents of the source file
     * @throws IOException problems while reading the source file or writing the output
     */
    public void render(final HTMLElement parent, final ISourceNode source,
                       final Reader contents) throws IOException {
        final HTMLElement pre = parent
                .pre(Styles.SOURCE + " lang-" + lang + " linenums");
        final BufferedReader lineBuffer = new BufferedReader(contents);

//        if (!hasLog) {
//            LogUtils.log("\n");
//            LogUtils.log("=================================================");
//            for (ClassInfo ci : CoverageBuilder.classInfos.values()) {
//                LogUtils.log("VITA ci className=" + ci.getClassName() + ", classFile=" + ci.getClassFile() + ", methods: ");
//                for (MethodInfo mi : ci.getMethodInfos().values()) {
//                    LogUtils.log("\t\t" + mi.getMethodName() + ", " + mi.getParameters());
//                }
//            }
//            LogUtils.logLine();
//            for (IClassCoverage cc : CoverageBuilder.classes.values()) {
//                LogUtils.log("VITA cc name=" + cc.getName() + ", packageName=" + cc.getPackageName() + ", sourceFileName=" + cc.getSourceFileName() + ", methods: ");
//                for (IMethodCoverage mc : cc.getMethods()) {
//                    LogUtils.log("\t\t" + mc.getName() + ", " + mc.getDesc());
//                }
//            }
//            LogUtils.log("=================================================");
//            LogUtils.log("\n");
//            hasLog = true;
//        }

        String sourceFilePath = ((SourceFileCoverageImpl) source).getPackageName() + "/" + source.getName();
        LogUtils.log("SourceHighlighter render: sourceFilePath = " + sourceFilePath);

        String lineSrc;
        int nr = 0;
        while ((lineSrc = lineBuffer.readLine()) != null) {
            nr++;
            renderCodeLine(pre, lineSrc, source.getLine(nr), nr, sourceFilePath);
        }
    }

    private static boolean hasLog = false;

    private void renderCodeLine(final HTMLElement pre, final String linesrc,
                                final ILine line, final int lineNr) throws IOException {
        highlight(pre, line, lineNr).text(linesrc);
        pre.text("\n");
    }

    private void renderCodeLine(final HTMLElement pre, final String linesrc,
                                final ILine line, final int lineNr, String sourceFilePath) throws IOException {
        if (containsFile(sourceFilePath)) {
            //	增量覆盖
            SourceInfo sourceInfo = Utils.getSourceInfo(sourceFilePath);
            if (sourceInfo != null) {
                //	新增的类
                if (StringUtils.equalsIgnoreCase(Type.ADD, sourceInfo.getType())) {
                    highlight(pre, line, lineNr).text("+ " + linesrc);
                    pre.text("\n");
                } else {
                    //	修改的类
                    boolean isAddLine = Utils.lineInAddLines(lineNr, sourceInfo);
                    if (isAddLine) {
                        highlight(pre, line, lineNr).text("+ " + linesrc);
                        pre.text("\n");
                    } else {
                        highlight(pre, line, lineNr, ICounter.EMPTY).text(" " + linesrc);
                        pre.text("\n");
                    }
                }
            } else {
                highlight(pre, line, lineNr).text(" " + linesrc);
                pre.text("\n");
            }
        } else {
            highlight(pre, line, lineNr).text(linesrc);
            pre.text("\n");
        }
    }

    private boolean containsFile(String path) {
        if (CoverageBuilder.classes == null || StringUtils.isNullOrEmpty(path)) {
            return false;
        }
        for (IClassCoverage cc : CoverageBuilder.classes.values()) {
            if (path.contains(cc.getPackageName() + "/" + cc.getSourceFileName())) {
                return true;
            }
        }
        return false;
    }

    HTMLElement highlight(final HTMLElement pre, final ILine line,
                          final int lineNr) throws IOException {
        return highlight(pre, line, lineNr, line.getStatus());
    }
    HTMLElement highlight(final HTMLElement pre, final ILine line,
                          final int lineNr, int lineStatus) throws IOException {
        final String style;
        switch (lineStatus) {
            case ICounter.NOT_COVERED:
                style = Styles.NOT_COVERED;
                break;
            case ICounter.FULLY_COVERED:
                style = Styles.FULLY_COVERED;
                break;
            case ICounter.PARTLY_COVERED:
                style = Styles.PARTLY_COVERED;
                break;
            default:
//                return pre;
                style = Styles.NORMAL;
                break;
        }

        return highlight(pre, line, lineNr, style);
    }

    private HTMLElement highlight(HTMLElement pre, ILine line, int lineNr, String style) throws IOException {
        final String lineId = "L" + Integer.toString(lineNr);
        final ICounter branches = line.getBranchCounter();
        switch (branches.getStatus()) {
            case ICounter.NOT_COVERED:
                return span(pre, lineId, style, Styles.BRANCH_NOT_COVERED,
                        "All %2$d branches missed.", branches);
            case ICounter.FULLY_COVERED:
                return span(pre, lineId, style, Styles.BRANCH_FULLY_COVERED,
                        "All %2$d branches covered.", branches);
            case ICounter.PARTLY_COVERED:
                return span(pre, lineId, style, Styles.BRANCH_PARTLY_COVERED,
                        "%1$d of %2$d branches missed.", branches);
            default:
                return pre.span(style, lineId);
        }
    }

    private HTMLElement span(final HTMLElement parent, final String id,
                             final String style1, final String style2, final String title,
                             final ICounter branches) throws IOException {
        final HTMLElement span = parent.span(style1 + " " + style2, id);
        final Integer missed = Integer.valueOf(branches.getMissedCount());
        final Integer total = Integer.valueOf(branches.getTotalCount());
        span.attr("title", String.format(locale, title, missed, total));
        return span;
    }

}
