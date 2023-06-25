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
package org.jacoco.report.internal.html.page;

import static java.lang.String.format;

import java.io.IOException;
import java.io.Reader;

import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.resources.Resources;

/**
 * Page showing the content of a source file with numbered and highlighted
 * source lines.
 */
public class SourceFilePage extends NodePage<ISourceNode> {

	private final Reader sourceReader;

	private final int tabWidth;

	/**
	 * Creates a new page with given information.
	 * 
	 * @param sourceFileNode
	 *            coverage data for this source file
	 * @param sourceReader
	 *            reader for the source code
	 * @param tabWidth
	 *            number of character per tab
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder for this page
	 * @param context
	 *            settings context
	 */
	public SourceFilePage(final ISourceNode sourceFileNode,
			final Reader sourceReader, final int tabWidth,
			final ReportPage parent, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(sourceFileNode, parent, folder, context);
		this.sourceReader = sourceReader;
		this.tabWidth = tabWidth;
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		final SourceHighlighter hl = new SourceHighlighter(context.getLocale());
		hl.render(body, getNode(), sourceReader);
		addAutoLocationScript(body);
		sourceReader.close();
	}

	/**
	 * 源码页面，添加自动定位到指定位置的script脚本
	 * 直接用href锚点定位有时会失败，这里增加补偿逻辑
	 */
	private void addAutoLocationScript(HTMLElement body) {
		try {
			HTMLElement script = body.element("script");
			script.text("function autoLoc() {\n" +
					"    const hash = location.hash\n" +
//					"	 console.log(hash)\n" +
					"    if (hash) {\n" +
					"        const link = document.createElement('a')\n" +
					"        link.setAttribute('href', hash)\n" +
					"        link.click()\n" +
					"    }\n" +
					"}\n" +
					"setTimeout('autoLoc()', 100)");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void head(final HTMLElement head) throws IOException {
		super.head(head);
		head.link("stylesheet", context.getResources().getLink(folder,
				Resources.PRETTIFY_STYLESHEET), "text/css");
		head.script(context.getResources().getLink(folder,
				Resources.PRETTIFY_SCRIPT));
	}

	@Override
	protected String getOnload() {
		return format("window['PR_TAB_WIDTH']=%d;prettyPrint()",
				Integer.valueOf(tabWidth));
	}

	@Override
	protected String getFileName() {
		return getNode().getName() + ".html";
	}

}
