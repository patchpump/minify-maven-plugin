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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.mozilla.javascript.EvaluatorException;

import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroup;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.samaxes.maven.minify.common.JavaScriptErrorReporter;
import com.samaxes.maven.minify.common.UglifyJS2Compiler;
import com.samaxes.maven.minify.common.UglifyJS3Compiler;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Task for merging and compressing JavaScript files.
 */
public class ProcessJSFilesTask extends ProcessFilesTask {

	/**
	 * Task constructor.
	 *
	 * @param opt task options
	 * @throws FileNotFoundException 
	 */
	public ProcessJSFilesTask(final TaskOptions opt) throws FileNotFoundException {
		super(opt);
	}

	/**
	 * Minifies a JavaScript file. Create missing parent directories if needed.
	 *
	 * @param mergedFile input file resulting from the merged step
	 * @param minifiedFile output file resulting from the minify step
	 * @throws IOException when the minify step fails
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void minify(File mergedFile, File minifiedFile) throws IOException {

		minifiedFile.getParentFile().mkdirs();

		File compressedFile = new File(minifiedFile.getAbsolutePath() + ".gz");
		OutputStream out = new FileOutputStream(minifiedFile);
		try (InputStream in = new FileInputStream(mergedFile);
			InputStreamReader reader = new InputStreamReader(in, opt.charset);
			OutputStreamWriter writer = new OutputStreamWriter(out, opt.charset)) {

			log.info("Creating the minified file [" + ((opt.verbose) ? minifiedFile.getPath() : minifiedFile.getName()) + "] using [" + opt.engine + "] engine.");

			switch (opt.engine) {
			case CLOSURE:
				Compiler compiler = new Compiler();
				CompilerOptions options = new CompilerOptions();
				opt.closureConfig.getCompilationLevel().setOptionsForCompilationLevel(options);
				options.setOutputCharset(Charset.forName(opt.charset));
				options.setLanguageIn(opt.closureConfig.getLanguage());
				options.setAngularPass(opt.closureConfig.getAngularPass());
				options.setDependencyOptions(opt.closureConfig.getDependencyOptions());
				if (opt.closureConfig.getWarningLevels() != null) {
					DiagnosticGroups diagnosticGroups = compiler.getDiagnosticGroups();
					for (Entry<String, String> entry : opt.closureConfig.getWarningLevels().entrySet()) {
						String groupName = entry.getKey();
						String levelName = entry.getValue();
						DiagnosticGroup diagnosticGroup = diagnosticGroups.forName(groupName);
						if (diagnosticGroup != null)
							options.setWarningLevel(diagnosticGroup, CheckLevel.valueOf(levelName));
						else
							log.warn("Unsupported diagnostic group [" + groupName + "] ignored.");
					}
				}

				File sourceMapResult = new File(minifiedFile.getPath() + ".map");
				if (opt.closureConfig.getSourceMapFormat() != null) {
					options.setSourceMapFormat(opt.closureConfig.getSourceMapFormat());
					options.setSourceMapOutputPath(sourceMapResult.getPath());
				}

				SourceFile input = SourceFile.fromInputStream(mergedFile.getName(), in);
				List<SourceFile> externs = opt.closureConfig.getExterns();
				if (opt.closureConfig.getUseDefaultExterns()) {
					externs.addAll(CommandLineRunner.getDefaultExterns());
				}

				compiler.compile(externs, Lists.newArrayList(input), options);

				if (compiler.hasErrors())
					throw new EvaluatorException(compiler.getErrors()[0].description);

				writer.append(compiler.toSource());

				if (opt.closureConfig.getSourceMapFormat() != null) {
					log.info("Creating the minified file map ["
						+ ((opt.verbose) ? sourceMapResult.getPath() : sourceMapResult.getName()) + "].");

					sourceMapResult.createNewFile();
					flushSourceMap(sourceMapResult, minifiedFile.getName(), compiler.getSourceMap());

					writer.append(System.getProperty("line.separator"));
					writer.append("//# sourceMappingURL=" + sourceMapResult.getName());
				}
				break;

			case YUI:
				JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new JavaScriptErrorReporter(log, mergedFile.getName()));
				compressor.compress(writer, opt.yuiConfig.getLineBreak(), opt.yuiConfig.isMunge(), opt.verbose,
					opt.yuiConfig.isPreserveSemicolons(), opt.yuiConfig.isDisableOptimizations());
				break;

			case UGLIFY2:
				new UglifyJS2Compiler().compile(reader, writer, new JavaScriptErrorReporter(log, mergedFile.getName()));
				break;

			case UGLIFY:
			case UGLIFY3:
				new UglifyJS3Compiler().compile(reader, writer, new JavaScriptErrorReporter(log, mergedFile.getName()));
				break;

			default:
				log.warn("JavaScript engine [" + opt.engine + "] not supported.");
				break;
			}

			writer.flush();

		} catch (Exception e) {
			close(out);
			minifiedFile.delete();
			compressedFile.delete();
			log.error("Failed to compress the JavaScript file [" + ((opt.verbose) ? mergedFile.getPath() : mergedFile.getName()) + "].", e);
			throw new IOException(e);

		} finally {
			close(out);
		}

		if (opt.gzip)
			gzip(minifiedFile, compressedFile);
	}

	private void flushSourceMap(File sourceMapOutputFile, String minifyFileName, SourceMap sourceMap) {
		try (FileWriter out = new FileWriter(sourceMapOutputFile)) {
			sourceMap.appendTo(out, minifyFileName);
		} catch (IOException e) {
			log.error("Failed to write the JavaScript Source Map file [" + sourceMapOutputFile.getName() + "].", e);
		}
	}
}
