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

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.DependencyOptions;
import com.google.javascript.jscomp.SourceFile;
import com.samaxes.maven.minify.common.Aggregation;
import com.samaxes.maven.minify.common.AggregationConfiguration;
import com.samaxes.maven.minify.common.ClosureConfig;
import com.samaxes.maven.minify.common.YuiConfig;

/**
 * Goal for combining and minifying CSS and JavaScript files.
 */
@Mojo(name = "minify", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class MinifyMojo extends AbstractMojo {

	public static enum Engine {
		YUI, CLOSURE, UGLIFY;
	}

	/**
	 * Display additional informational messages and warnings.
	 */
	@Parameter(property = "verbose", defaultValue = "false")
	private boolean verbose;

	/**
	 * Display even more additional informational messages.
	 */
	@Parameter(property = "debug", defaultValue = "false")
	private boolean debug;

	/**
	 * Display even more additional informational messages.
	 */
	@Parameter(property = "incrementalBuild", defaultValue = "true")
	private boolean incrementalBuild;

	/**
	 * Size of the buffer used to read source files.
	 */
	@Parameter(property = "bufferSize", defaultValue = "4096")
	private int bufferSize;

	/**
	 * If a supported character set is specified, it will be used to read the input file. Otherwise, it will assume that
	 * the platform's default character set is being used. The output file is encoded using the same character set.<br/>
	 * See the <a href="http://www.iana.org/assignments/character-sets">IANA Charset Registry</a> for a list of valid
	 * encoding types.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "charset", defaultValue = "${project.build.sourceEncoding}")
	private String charset;

	/**
	 * The output file name suffix.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "suffix", defaultValue = "min")
	private String suffix;

	/**
	 * Do not append a suffix to the minified output file name, independently of the value in the {@code suffix}
	 * parameter.<br/>
	 * <strong>Warning:</strong> when both the options {@code nosuffix} and {@code skipMerge} are set to {@code true},
	 * the plugin execution phase needs to be set to {@code package}, otherwise the output files will be overridden by
	 * the source files during the packaging.
	 *
	 * @since 1.7
	 */
	@Parameter(property = "nosuffix", defaultValue = "false")
	private boolean nosuffix;

	/**
	 * Skip the merge step. Minification will be applied to each source file individually.
	 *
	 * @since 1.5.2
	 */
	@Parameter(property = "skipMerge", defaultValue = "false")
	private boolean skipMerge;

	/**
	 * Skip the minify step. Useful when merging files that are already minified.
	 *
	 * @since 1.5.2
	 */
	@Parameter(property = "skipMinify", defaultValue = "false")
	private boolean skipMinify;

	/**
	 * Gzip target file into targetfile.gz
	 *
	 * @since 1.5.7
	 */
	@Parameter(property = "gzip", defaultValue = "false")
	private boolean gzip;

	/**
	 * Webapp source directory.
	 */
	@Parameter(property = "webappSourceDir", defaultValue = "${basedir}/src/main/webapp")
	private String webappSourceDir;

	/**
	 * Webapp target directory.
	 */
	@Parameter(property = "webappTargetDir", defaultValue = "${project.build.directory}/${project.build.finalName}")
	private String webappTargetDir;

	/**
	 * Specify aggregations in an external JSON formatted config file.
	 *
	 * @since 1.7.5
	 */
	@Parameter(property = "bundleConfiguration")
	private String bundleConfiguration;

	/**
	 * CSS source directory.
	 */
	@Parameter(property = "cssSourceDir", defaultValue = "")
	private String cssSourceDir;

	/**
	 * CSS source include directory.
	 */
	@Parameter(property = "cssSourceIncludeDir", defaultValue = "")
	private String cssSourceIncludeDir;

	/**
	 * CSS source file names list.
	 */
	@Parameter(property = "cssSourceFiles", alias = "cssFiles")
	private ArrayList<String> cssSourceFiles;

	/**
	 * CSS files to include. Specified as fileset patterns which are relative to the CSS source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "cssSourceIncludes", alias = "cssIncludes")
	private ArrayList<String> cssSourceIncludes;

	/**
	 * CSS files to exclude. Specified as fileset patterns which are relative to the CSS source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "cssSourceExcludes", alias = "cssExcludes")
	private ArrayList<String> cssSourceExcludes;

	/**
	 * CSS target directory. Takes the same value as {@code cssSourceDir} when empty.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "cssTargetDir")
	private String cssTargetDir;

	/**
	 * CSS output file name.
	 */
	@Parameter(property = "cssFinalFile", defaultValue = "style.css")
	private String cssFinalFile;

	/**
	 * Define the CSS compressor engine to use.<br/>
	 * Possible values are:
	 * <ul>
	 * <li>{@code YUI}: <a href="http://yui.github.io/yuicompressor/">YUI Compressor</a></li>
	 * </ul>
	 *
	 * @since 1.7.1
	 */
	@Parameter(property = "cssEngine", defaultValue = "YUI")
	private Engine cssEngine;

	/**
	 * JavaScript source directory.
	 */
	@Parameter(property = "jsSourceDir", defaultValue = "")
	private String jsSourceDir;

	/**
	 * JavaScript source directory for jsSourceIncludes.
	 */
	@Parameter(property = "jsSourceIncludeDir", defaultValue = "")
	private String jsSourceIncludeDir;

	/**
	 * JavaScript source file names list.
	 */
	@Parameter(property = "jsSourceFiles", alias = "jsFiles")
	private ArrayList<String> jsSourceFiles;

	/**
	 * JavaScript files to include. Specified as fileset patterns which are relative to the JavaScript source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "jsSourceIncludes", alias = "jsIncludes")
	private ArrayList<String> jsSourceIncludes;

	/**
	 * JavaScript files to exclude. Specified as fileset patterns which are relative to the JavaScript source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "jsSourceExcludes", alias = "jsExcludes")
	private ArrayList<String> jsSourceExcludes;

	/**
	 * JavaScript target directory. Takes the same value as {@code jsSourceDir} when empty.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "jsTargetDir")
	private String jsTargetDir;

	/**
	 * JavaScript output file name.
	 */
	@Parameter(property = "jsFinalFile", defaultValue = "script.js")
	private String jsFinalFile;

	/**
	 * Define the JavaScript compressor engine to use.<br/>
	 * Possible values are:
	 * <ul>
	 * <li>{@code YUI}: <a href="http://yui.github.io/yuicompressor/">YUI Compressor</a></li>
	 * <li>{@code CLOSURE}: <a href="https://developers.google.com/closure/compiler/">Google Closure Compiler</a></li>
	 * </ul>
	 *
	 * @since 1.6
	 */
	@Parameter(property = "jsEngine", defaultValue = "YUI")
	private Engine jsEngine;

	/**
	 * Some source control tools don't like files containing lines longer than, say 8000 characters. The line-break
	 * option is used in that case to split long lines after a specific column. It can also be used to make the code
	 * more readable and easier to debug. Specify {@code 0} to get a line break after each semi-colon in JavaScript, and
	 * after each rule in CSS. Specify {@code -1} to disallow line breaks.
	 */
	@Parameter(property = "yuiLineBreak", defaultValue = "-1")
	private int yuiLineBreak;

	/**
	 * Minify only. Do not obfuscate local symbols.
	 */
	@Parameter(property = "yuiNoMunge", defaultValue = "false")
	private boolean yuiNoMunge;

	/**
	 * Preserve unnecessary semicolons (such as right before a '}'). This option is useful when compressed code has to
	 * be run through JSLint.
	 */
	@Parameter(property = "yuiPreserveSemicolons", defaultValue = "false")
	private boolean yuiPreserveSemicolons;

	/**
	 * Disable all the built-in micro-optimizations.
	 */
	@Parameter(property = "yuiDisableOptimizations", defaultValue = "false")
	private boolean yuiDisableOptimizations;

	/**
	 * Refers to which version of ECMAScript to assume when checking for errors in your code.<br/>
	 * Possible values are:
	 * <ul>
	 * <li>{@code ECMASCRIPT3}: Checks code assuming ECMAScript 3 compliance.</li>
	 * <li>{@code ECMASCRIPT5}: Checks code assuming ECMAScript 5 compliance (default).</li>
	 * <li>{@code ECMASCRIPT5_STRICT}: Like {@code ECMASCRIPT5} but assumes compliance with strict mode ('use strict';).</li>
	 * <li>{@code ECMASCRIPT_2015}: ECMAScript standard approved in 2015.</li>
	 * <li>{@code ECMASCRIPT6_TYPED}: A superset of ES6 which adds Typescript-style type declarations. Always strict.</li>
	 * <li>{@code ECMASCRIPT_2016}: ECMAScript standard approved in 2016.</li>
	 * <li>{@code ECMASCRIPT_2017}: ECMAScript standard approved in 2017.</li>
	 * <li>{@code ECMASCRIPT_NEXT}: ECMAScript latest draft standard.</li>
	 * </ul>
	 *
	 * @since 1.7.2
	 */
	@Parameter(property = "closureLanguage", defaultValue = "ECMASCRIPT5")
	private LanguageMode closureLanguage;

	/**
	 * The degree of compression and optimization to apply to your JavaScript.<br/>
	 * There are three possible compilation levels:
	 * <ul>
	 * <li>{@code WHITESPACE_ONLY}: Just removes whitespace and comments from your JavaScript.</li>
	 * <li>{@code SIMPLE_OPTIMIZATIONS}: Performs compression and optimization that does not interfere with the
	 * interaction between the compiled JavaScript and other JavaScript. This level renames only local variables.</li>
	 * <li>{@code ADVANCED_OPTIMIZATIONS}: Achieves the highest level of compression by renaming symbols in your
	 * JavaScript. When using {@code ADVANCED_OPTIMIZATIONS} compilation you must perform extra steps to preserve
	 * references to external symbols. See <a href="/closure/compiler/docs/api-tutorial3">Advanced Compilation and
	 * Externs</a> for more information about {@code ADVANCED_OPTIMIZATIONS}.</li>
	 * </ul>
	 *
	 * @since 1.7.2
	 */
	@Parameter(property = "closureCompilationLevel", defaultValue = "SIMPLE_OPTIMIZATIONS")
	private CompilationLevel closureCompilationLevel;

	/**
	 * @since patchpump-R5
	 */
	@Parameter(property = "closureWarningLevels")
	private Map<String, String> closureWarningLevels;

	/**
	 * List of JavaScript files containing code that declares function names or other symbols. Use
	 * {@code closureExterns} to preserve symbols that are defined outside of the code you are compiling. The
	 * {@code closureExterns} parameter only has an effect if you are using a {@code CompilationLevel} of
	 * {@code ADVANCED_OPTIMIZATIONS}.<br/>
	 * These file names are relative to {@link #webappSourceDir} directory.
	 *
	 * @since 1.7.2
	 */
	@Parameter(property = "closureExterns")
	private ArrayList<String> closureExterns;

	/**
	 * <p>
	 * Use default externs provided with Closure Compiler.
	 * </p>
	 * <p>
	 * For the complete list of externs please visit:<br />
	 * <a href="https://github.com/google/closure-compiler/tree/master/externs">https://github.com/google/closure-
	 * compiler/tree/master/externs</a>
	 * </p>
	 *
	 * @since 1.7.4
	 */
	@Parameter(property = "closureUseDefaultExterns", defaultValue = "false")
	private boolean closureUseDefaultExterns;

	/**
	 * <p>
	 * Collects information mapping the generated (compiled) source back to its original source for debugging purposes.
	 * </p>
	 * <p>
	 * Please visit <a
	 * href="https://docs.google.com/document/d/1U1RGAehQwRypUTovF1KRlpiOFze0b-_2gc6fAH0KY0k/edit">Source Map Revision 3
	 * Proposal</a> for more information.
	 * </p>
	 *
	 * @since 1.7.3
	 */
	@Parameter(property = "closureCreateSourceMap", defaultValue = "false")
	private boolean closureCreateSourceMap;

	/**
	 * <p>
	 * Enables or disables sorting mode for Closure Library dependencies.
	 * </p>
	 * <p>
	 * If true, automatically sort dependencies so that a file that {@code goog.provides} symbol X will always come
	 * before a file that {@code goog.requires} symbol X.
	 * </p>
	 *
	 * @since 1.7.4
	 */
	@Parameter(property = "closureSortDependencies", defaultValue = "false")
	private boolean closureSortDependencies;

	/**
	 * Generate {@code $inject} properties for AngularJS for functions annotated with {@code @ngInject}.
	 *
	 * @since 1.7.3
	 */
	@Parameter(property = "closureAngularPass", defaultValue = "false")
	private boolean closureAngularPass;

	/**
	 * Executed when the goal is invoked, it will first invoke a parallel lifecycle, ending at the given phase.
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skipMerge && skipMinify) {
			getLog().warn("Both merge and minify steps are configured to be skipped.");
			return;
		}

		fillOptionalValues();

		YuiConfig yuiConfig = fillYuiConfig();
		ClosureConfig closureConfig = fillClosureConfig();
		Collection<ProcessFilesTask> processFilesTasks;
		try {
			processFilesTasks = createTasks(yuiConfig, closureConfig);
		} catch (FileNotFoundException e) {
			throw new MojoFailureException(e.getMessage(), e);
		}

		ExecutorService executor = Executors.newFixedThreadPool(processFilesTasks.size());
		try {
			List<Future<Object>> futures = executor.invokeAll(processFilesTasks);
			for (Future<Object> future : futures) {
				try {
					future.get();
				} catch (ExecutionException e) {
					throw new MojoExecutionException(e.getMessage(), e);
				}
			}
			executor.shutdown();
		} catch (InterruptedException e) {
			executor.shutdownNow();
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private void fillOptionalValues() {
		if (Strings.isNullOrEmpty(cssTargetDir))
			cssTargetDir = cssSourceDir;

		if (Strings.isNullOrEmpty(jsTargetDir))
			jsTargetDir = jsSourceDir;

		if (Strings.isNullOrEmpty(jsSourceIncludeDir))
			jsSourceIncludeDir = jsSourceDir;

		if (Strings.isNullOrEmpty(cssSourceIncludeDir))
			cssSourceIncludeDir = cssSourceDir;

		if (Strings.isNullOrEmpty(charset))
			charset = Charset.defaultCharset().name();

		if(debug)
			verbose = true;
	}

	private YuiConfig fillYuiConfig() {
		return new YuiConfig(yuiLineBreak, yuiNoMunge, yuiPreserveSemicolons, yuiDisableOptimizations);
	}

	private ClosureConfig fillClosureConfig() {
		
		DependencyOptions dependencyOptions = closureSortDependencies ? DependencyOptions.sortOnly() : DependencyOptions.none();
		List<SourceFile> externs = new ArrayList<>();
		for (String extern : closureExterns)
			externs.add(SourceFile.fromFile(webappSourceDir + File.separator + extern, Charset.forName(charset)));

		return new ClosureConfig(closureLanguage, closureCompilationLevel, dependencyOptions, externs,
			closureWarningLevels, closureUseDefaultExterns, closureCreateSourceMap, closureAngularPass);
	}

	private Collection<ProcessFilesTask> createTasks(YuiConfig yuiConfig, ClosureConfig closureConfig)
		throws MojoFailureException, FileNotFoundException {
		List<ProcessFilesTask> tasks = newArrayList();
		if (!Strings.isNullOrEmpty(bundleConfiguration)) {
			AggregationConfiguration aggregationConfiguration;
			try (Reader bundleConfigurationReader = new FileReader(bundleConfiguration)) {
				aggregationConfiguration = new Gson().fromJson(bundleConfigurationReader,
					AggregationConfiguration.class);
			} catch (IOException e) {
				throw new MojoFailureException(
					"Failed to open the bundle configuration file [" + bundleConfiguration + "].", e);
			}

			for (Aggregation aggregation : aggregationConfiguration.getBundles()) {
				if (Aggregation.AggregationType.css.equals(aggregation.getType()))
					tasks.add(createCSSTask(yuiConfig, closureConfig, aggregation.getFiles(),
						Collections.<String> emptyList(), Collections.<String> emptyList(), aggregation.getName()));
				else if (Aggregation.AggregationType.js.equals(aggregation.getType()))
					tasks.add(createJSTask(yuiConfig, closureConfig, aggregation.getFiles(),
						Collections.<String> emptyList(), Collections.<String> emptyList(), aggregation.getName()));
			}
		} else {
			tasks.add(createCSSTask(yuiConfig, closureConfig, cssSourceFiles, cssSourceIncludes, cssSourceExcludes,
				cssFinalFile));
			tasks.add(
				createJSTask(yuiConfig, closureConfig, jsSourceFiles, jsSourceIncludes, jsSourceExcludes, jsFinalFile));
		}
		return tasks;
	}

	private ProcessFilesTask createCSSTask(YuiConfig yuiConfig, ClosureConfig closureConfig,
		List<String> cssSourceFiles, List<String> cssSourceIncludes, List<String> cssSourceExcludes,
		String cssFinalFile) throws FileNotFoundException {

		TaskOptions opt = new TaskOptions(getLog(), verbose, debug, incrementalBuild, bufferSize, charset, suffix, nosuffix, skipMerge,
			skipMinify, webappSourceDir, webappTargetDir, cssSourceDir, cssSourceIncludeDir, cssSourceFiles,
			cssSourceIncludes, cssSourceExcludes, cssTargetDir, cssFinalFile, cssEngine, yuiConfig, gzip);

		return new ProcessCSSFilesTask(opt);
	}

	private ProcessFilesTask createJSTask(YuiConfig yuiConfig, ClosureConfig closureConfig, List<String> jsSourceFiles,
		List<String> jsSourceIncludes, List<String> jsSourceExcludes, String jsFinalFile) throws FileNotFoundException {

		TaskOptions opt = new TaskOptions(getLog(), verbose, debug, incrementalBuild, bufferSize, charset, suffix, nosuffix, skipMerge,
			skipMinify, webappSourceDir, webappTargetDir, jsSourceDir, jsSourceIncludeDir, jsSourceFiles,
			jsSourceIncludes, jsSourceExcludes, jsTargetDir, jsFinalFile, jsEngine, yuiConfig, closureConfig, gzip);

		return new ProcessJSFilesTask(opt);
	}
}
