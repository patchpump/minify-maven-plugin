/*
 * Minify Maven Plugin
 * https://github.com/samaxes/minify-maven-plugin
 *
 * Copyright (c) 2009 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.maven.minify.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import com.samaxes.maven.minify.common.SourceFilesEnumeration;

/**
 * Abstract class for merging and compressing a files list.
 */
public abstract class ProcessFilesTask implements Callable<Object> {

	private static final String TEMP_SUFFIX = ".tmp";

	final Log log;
	final File sourceDir;
	final File targetDir;
	final File sourceIncludeDir;
	final List<File> files = new ArrayList<>();
	final boolean sourceFilesEmpty;
	final boolean sourceIncludesEmpty;

	final TaskOptions opt;

	/**
	 * Task constructor.
	 *
	 * @param opt task options
	 * @throws FileNotFoundException 
	 */
	public ProcessFilesTask(final TaskOptions opt) throws FileNotFoundException {

		this.opt = opt;
		this.log = opt.log;

		this.sourceDir = appendRelativePath(opt.webappSourceDir, opt.sourceDir);
		this.targetDir = appendRelativePath(opt.webappTargetDir, opt.targetDir);
		this.sourceIncludeDir = appendRelativePath(opt.webappTargetDir, opt.sourceIncludeDir);

		if(opt.debug) {
			log.info(opt.toString());
			log.info("sourceDir:" + sourceDir);
			log.info("targetDir:" + targetDir);
			log.info("sourceIncludeDir:" + sourceIncludeDir);
		}
		
		for (String sourceFilename : opt.sourceFiles)
			addNewSourceFile(opt.mergedFilename, sourceFilename);

		for (File sourceInclude : getFilesToInclude(opt.sourceIncludes, opt.sourceExcludes)) {
			if (!files.contains(sourceInclude))
				addNewSourceFile(opt.mergedFilename, sourceInclude);
		}

		this.sourceFilesEmpty = opt.sourceFiles.isEmpty();
		this.sourceIncludesEmpty = opt.sourceIncludes.isEmpty();
	}

	private File appendRelativePath(String base, String relative) {
		File file = new File(base);
		if(relative != null && !relative.isEmpty() && !".".equals(relative))
			file = new File(file, relative);
		return file.getAbsoluteFile();
	}
	
	/**
	 * Method executed by the thread.
	 *
	 * @throws IOException when the merge or minify steps fail
	 */
	@Override
	public Object call() throws IOException {

		synchronized (log) {
			log.info("");
			if(opt.debug)
				log.info("Skip steps:" + opt.skipMerge + " " + opt.skipMinify);
			
			String sourceBasePath = sourceDir.getAbsolutePath();

			if (!files.isEmpty() && (targetDir.exists() || targetDir.mkdirs())) {
				if (opt.skipMerge) {
					log.info("Starting " + opt.type + " [minify] task:");
					for (File file : files) {
						String fileName = file.getName();
						String baseName = FileUtils.basename(file.getName());
						String extension = FileUtils.getExtension(file.getName());
						String originalPath = file.getAbsolutePath();
						String subPath = originalPath.substring(sourceBasePath.length(), originalPath.lastIndexOf(File.separator));

						File targetPath = new File(targetDir, subPath);
						targetPath.mkdirs();

						File minifiedFile = new File(targetPath, (opt.nosuffix) ? fileName : baseName + opt.suffix + "." + extension);
						minify(file, minifiedFile);
					}

				} else if (opt.skipMinify) {
					log.info("Starting " + opt.type + " [merge] task:");
					File mergedFile = new File(targetDir, opt.mergedFilename);
					merge(mergedFile);

				} else {
					log.info("Starting " + opt.type + " [merge, minify] task:");
					File mergedFile = new File(targetDir, (opt.nosuffix) ? opt.mergedFilename + TEMP_SUFFIX : opt.mergedFilename);
					File taretFile = new File(targetDir, opt.mergedFilename);
					String extension = FileUtils.getExtension(taretFile.getName());
					String baseName = FileUtils.basename(taretFile.getName());
					File minifiedFile = new File(taretFile.getParentFile(), (opt.nosuffix) ? opt.mergedFilename : baseName + opt.suffix + "." + extension);
					if(opt.debug) {
						log.info("targetDir: " + targetDir);
						log.info("mergedFilename: " + opt.mergedFilename);
						log.info("mergedFile: " + mergedFile);
						log.info("minifiedFile: " + minifiedFile);
					}
					merge(mergedFile);
					minify(mergedFile, minifiedFile);
					if (opt.nosuffix) {
						if (!mergedFile.delete())
							mergedFile.deleteOnExit();
					}
				}

			} else if (!sourceFilesEmpty || !sourceIncludesEmpty) {
				log.error("No valid " + opt.type + " source files found to process.");
			}
		}
		return null;
	}

	/**
	 * Merges a list of source files. Create missing parent directories if needed.
	 *
	 * @param mergedFile output file resulting from the merged step
	 * @throws IOException when the merge step fails
	 */
	protected void merge(File mergedFile) throws IOException {
		mergedFile.getParentFile().mkdirs();
		try (InputStream sequence = new SequenceInputStream(new SourceFilesEnumeration(log, files, opt.verbose));
			OutputStream out = new FileOutputStream(mergedFile);
			InputStreamReader sequenceReader = new InputStreamReader(sequence, opt.charset);
			OutputStreamWriter outWriter = new OutputStreamWriter(out, opt.charset)) {
			log.info("Creating the merged file [" + ((opt.verbose) ? mergedFile.getPath() : mergedFile.getName()) + "].");
			IOUtil.copy(sequenceReader, outWriter, opt.bufferSize);
		}
	}

	/**
	 * Minifies a source file. Create missing parent directories if needed.
	 *
	 * @param mergedFile input file resulting from the merged step
	 * @param minifiedFile output file resulting from the minify step
	 * @throws IOException when the minify step fails
	 */
	abstract void minify(File mergedFile, File minifiedFile) throws IOException;

	/**
	 * Compress the minifed target file.
	 *
	 * @param mergedFile input file resulting from the merged step
	 * @param minifiedFile output file resulting from the minify step
	 */

	void gzip(File mergedFile, File minifiedFile) {
		if (!opt.gzip)
			return;

		try {
			File compressedFile = new File(minifiedFile.getAbsolutePath() + ".gz");
			try (InputStream in = new FileInputStream(minifiedFile);
				OutputStream out = new FileOutputStream(compressedFile);
				GZIPOutputStream outGZIP = new GZIPOutputStream(out)) {
				IOUtil.copy(in, outGZIP, opt.bufferSize);
			}

			log.info("Uncompressed size: " + mergedFile.length() + " bytes.");
			log.info("Compressed size: " + minifiedFile.length() + " bytes minified (" + compressedFile.length()
				+ " bytes gzipped).");

		} catch (IOException e) {
			log.debug("Failed to calculate the gzipped file size.", e);
		}
	}

	/**
	 * Logs an addition of a new source file.
	 *
	 * @param finalFilename the final file name
	 * @param sourceFilename the source file name
	 * @throws FileNotFoundException when the given source file does not exist
	 */
	private void addNewSourceFile(String finalFilename, String sourceFilename) throws FileNotFoundException {
		File sourceFile = new File(sourceDir, sourceFilename);
		addNewSourceFile(finalFilename, sourceFile);
	}

	/**
	 * Logs an addition of a new source file.
	 *
	 * @param finalFilename the final file name
	 * @param sourceFile the source file
	 * @throws FileNotFoundException when the given source file does not exist
	 */
	private void addNewSourceFile(String finalFilename, File sourceFile) throws FileNotFoundException {

		if (!sourceFile.exists())
			throw new FileNotFoundException("The source file ["
				+ ((opt.verbose) ? sourceFile.getPath() : sourceFile.getName()) + "] does not exist.");

		if (finalFilename.equalsIgnoreCase(sourceFile.getName()))
			log.warn("The source file [" + ((opt.verbose) ? sourceFile.getPath() : sourceFile.getName())
				+ "] has the same name as the final file.");

		log.debug("Adding source file [" + ((opt.verbose) ? sourceFile.getPath() : sourceFile.getName()) + "].");
		files.add(sourceFile);
	}

	/**
	 * Returns the files to copy. Default exclusions are used when the excludes list is empty.
	 *
	 * @param includes list of source files to include
	 * @param excludes list of source files to exclude
	 * @return the files to copy
	 */
	private List<File> getFilesToInclude(List<String> includes, List<String> excludes) {

		List<File> includedFiles = new ArrayList<File>();

		if (includes != null && !includes.isEmpty()) {

			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setIncludes(includes.toArray(new String[0]));
			scanner.setExcludes(excludes.toArray(new String[0]));
			scanner.addDefaultExcludes();
			scanner.setBasedir(sourceIncludeDir);
			scanner.scan();

			for (String includedFilename : scanner.getIncludedFiles())
				includedFiles.add(new File(sourceIncludeDir, includedFilename));

			Collections.sort(includedFiles, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
		}

		return includedFiles;
	}
}
