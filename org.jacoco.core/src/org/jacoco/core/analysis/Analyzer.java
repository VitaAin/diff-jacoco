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
package org.jacoco.core.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.analysis.ClassAnalyzer;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.utils.LogUtils;
import org.jacoco.core.utils.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An {@link Analyzer} instance processes a set of Java class files and
 * calculates coverage data for them. For each class file the result is reported
 * to a given {@link ICoverageVisitor} instance. In addition the
 * {@link Analyzer} requires a {@link ExecutionDataStore} instance that holds
 * the execution data for the classes to analyze. The {@link Analyzer} offers
 * several methods to analyze classes from a variety of sources.
 */
public class Analyzer {

	private final ExecutionDataStore executionData;

	private final ICoverageVisitor coverageVisitor;

	private final StringPool stringPool;

	/**
	 * Creates a new analyzer reporting to the given output.
	 * 
	 * @param executionData
	 *            execution data
	 * @param coverageVisitor
	 *            the output instance that will coverage data for every analyzed
	 *            class
	 */
	public Analyzer(final ExecutionDataStore executionData,
			final ICoverageVisitor coverageVisitor) {
		this.executionData = executionData;
		this.coverageVisitor = coverageVisitor;
		this.stringPool = new StringPool();
	}

	/**
	 * Creates an ASM class visitor for analysis.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @param className
	 *            VM name of the class
	 * @return ASM visitor to write class definition to
	 */
	private ClassVisitor createAnalyzingVisitor(final long classid,
			final String className) {
		final ExecutionData data = executionData.get(classid);
		final boolean[] probes;
		final boolean noMatch;
		if (data == null) {
			probes = null;
			noMatch = executionData.contains(className);
		} else {
			probes = data.getProbes();
			noMatch = false;
		}
		final ClassCoverageImpl coverage = new ClassCoverageImpl(className, classid, noMatch);
		LogUtils.log(getClass(), "createAnalyzingVisitor", "classid = " + classid + ", className = " + className
				+ ", noMatch = " + noMatch);

//		LogUtils.log(getClass(), "createAnalyzingVisitor", "coverage: pkg=" + coverage.getPackageName()
//				+ ", name=" + coverage.getName() + ", srcFileName=" + coverage.getSourceFileName());
//		if (coverage.getMethods() != null) {
//			for (IMethodCoverage methodCv : coverage.getMethods()) {
//				LogUtils.log(getClass(), "createAnalyzingVisitor", "methodCv: name=" + methodCv.getName()
//						+ ", desc=" + methodCv.getDesc() + ", sign=" + methodCv.getSignature());
//			}
//		}
//		LogUtils.log(getClass(), "createAnalyzingVisitor", "className = " + className + ">>>>>>> end >>>>>>>");
		final ClassAnalyzer analyzer = new ClassAnalyzer(coverage, probes,
				stringPool) {
			@Override
			public void visitSource(String source, String debug) {
				super.visitSource(source, debug);
				LogUtils.log(Analyzer.class, "visitSource", "source = " + source + ", coverage = " + coverage.getPackageName());
			}

			@Override
			public MethodProbesVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				LogUtils.log(Analyzer.class, "visitMethod", "name = " + name + ", desc = " + desc + ", coverage = " + coverage.getPackageName());
				return super.visitMethod(access, name, desc, signature, exceptions);
			}

			@Override
			public void visitEnd() {
				super.visitEnd();
				LogUtils.log(Analyzer.class, "visitEnd", "coverage = " + coverage.getPackageName() + "/" + coverage.getSourceFileName());
				coverageVisitor.visitCoverage(coverage);
			}
		};
		return new ClassProbesAdapter(analyzer, false);
	}

	private boolean analyzeClass(final byte[] source) {
		final long classId = CRC64.classId(source);
		final ClassReader reader = InstrSupport.classReaderFor(source);
		LogUtils.logWrap();
		LogUtils.logLine();
		LogUtils.log(getClass(), "analyzeClass", reader.getClassName());
		if ((reader.getAccess() & Opcodes.ACC_MODULE) != 0) {
			return false;
		}
		if ((reader.getAccess() & Opcodes.ACC_SYNTHETIC) != 0) {
			return false;
		}
		final ClassVisitor visitor = createAnalyzingVisitor(classId,
				reader.getClassName());
		if (visitor != null) {
			reader.accept(visitor, 0);
		}
		return visitor != null;
	}

	/**
	 * Analyzes the class definition from a given in-memory buffer.
	 * 
	 * @param buffer
	 *            class definitions
	 * @param location
	 *            a location description used for exception messages
	 * @throws IOException
	 *             if the class can't be analyzed
	 */
	public boolean analyzeClass(final byte[] buffer, final String location)
			throws IOException {
		try {
			return analyzeClass(buffer);
		} catch (final RuntimeException cause) {
			throw analyzerError(location, cause);
		}
	}

	/**
	 * Analyzes the class definition from a given input stream. The provided
	 * {@link InputStream} is not closed by this method.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param location
	 *            a location description used for exception messages
	 * @throws IOException
	 *             if the stream can't be read or the class can't be analyzed
	 */
	public boolean analyzeClass(final InputStream input, final String location)
			throws IOException {
		final byte[] buffer;
		try {
			buffer = InputStreams.readFully(input);
		} catch (final IOException e) {
			throw analyzerError(location, e);
		}
		return analyzeClass(buffer, location);
	}

	private IOException analyzerError(final String location,
			final Exception cause) {
		final IOException ex = new IOException(
				String.format("Error while analyzing %s.", location));
		ex.initCause(cause);
		return ex;
	}

	/**
	 * Analyzes all classes found in the given input stream. The input stream
	 * may either represent a single class file, a ZIP archive, a Pack200
	 * archive or a gzip stream that is searched recursively for class files.
	 * All other content types are ignored. The provided {@link InputStream} is
	 * not closed by this method.
	 * 
	 * @param input
	 *            input data
	 * @param location
	 *            a location description used for exception messages
	 * @return number of class files found
	 * @throws IOException
	 *             if the stream can't be read or a class can't be analyzed
	 */
	public int analyzeAll(final InputStream input, final String location)
			throws IOException {
//		LogUtils.log(getClass(), "analyzeAll", "location = " + location);
		final ContentTypeDetector detector;
		try {
			detector = new ContentTypeDetector(input);
		} catch (final IOException e) {
			throw analyzerError(location, e);
		}
		switch (detector.getType()) {
			case ContentTypeDetector.CLASSFILE:
				boolean res = analyzeClass(detector.getInputStream(), location);
				// todo vita 看下这里数量要不要改：：暂时不用改
				return res ? 1 : 0;
			case ContentTypeDetector.ZIPFILE:
				return analyzeZip(detector.getInputStream(), location);
			case ContentTypeDetector.GZFILE:
				return analyzeGzip(detector.getInputStream(), location);
			case ContentTypeDetector.PACK200FILE:
				return analyzePack200(detector.getInputStream(), location);
			default:
				return 0;
		}
	}

	/**
	 * Analyzes all class files contained in the given file or folder. Class
	 * files as well as ZIP files are considered. Folders are searched
	 * recursively.
	 * 
	 * @param file
	 *            file or folder to look for class files
	 * @return number of class files found
	 * @throws IOException
	 *             if the file can't be read or a class can't be analyzed
	 */
	public int analyzeAll(final File file) throws IOException {
		int count = 0;
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				count += analyzeAll(f);
			}
		} else {
			if (!file.exists()) {
				return 0;
			}
			// 排除R.class
			if (StringUtils.equals("R.class", file.getName()) || file.getName().contains("R$")) {
				return 0;
			}
			final InputStream in = new FileInputStream(file);
			try {
				count += analyzeAll(in, file.getPath());
			} finally {
				in.close();
			}
		}
		if (count > 0) {
			LogUtils.log(getClass(), "analyzeAll", file.getName() + " > count >> " + count);
		}
		return count;
	}

	/**
	 * Analyzes all classes from the given class path. Directories containing
	 * class files as well as archive files are considered.
	 * 
	 * @param path
	 *            path definition
	 * @param basedir
	 *            optional base directory, if <code>null</code> the current
	 *            working directory is used as the base for relative path
	 *            entries
	 * @return number of class files found
	 * @throws IOException
	 *             if a file can't be read or a class can't be analyzed
	 */
	public int analyzeAll(final String path, final File basedir)
			throws IOException {
		int count = 0;
		final StringTokenizer st = new StringTokenizer(path,
				File.pathSeparator);
		while (st.hasMoreTokens()) {
			count += analyzeAll(new File(basedir, st.nextToken()));
		}
		return count;
	}

	private int analyzeZip(final InputStream input, final String location)
			throws IOException {
		final ZipInputStream zip = new ZipInputStream(input);
		ZipEntry entry;
		int count = 0;
		while ((entry = nextEntry(zip, location)) != null) {
			count += analyzeAll(zip, location + "@" + entry.getName());
		}
		return count;
	}

	private ZipEntry nextEntry(final ZipInputStream input,
			final String location) throws IOException {
		try {
			return input.getNextEntry();
		} catch (final IOException e) {
			throw analyzerError(location, e);
		}
	}

	private int analyzeGzip(final InputStream input, final String location)
			throws IOException {
		GZIPInputStream gzipInputStream;
		try {
			gzipInputStream = new GZIPInputStream(input);
		} catch (final IOException e) {
			throw analyzerError(location, e);
		}
		return analyzeAll(gzipInputStream, location);
	}

	private int analyzePack200(final InputStream input, final String location)
			throws IOException {
		InputStream unpackedInput;
		try {
			unpackedInput = Pack200Streams.unpack(input);
		} catch (final IOException e) {
			throw analyzerError(location, e);
		}
		return analyzeAll(unpackedInput, location);
	}

}
