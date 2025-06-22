package com.samaxes.maven.minify.plugin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.DependencyOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.jarjar.com.google.common.base.Strings;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.samaxes.maven.minify.common.ClosureConfig;
import com.samaxes.maven.minify.common.YuiConfig;

public abstract class AbstractMinifyMojo extends AbstractMojo {

	public static enum Engine {
		 CLOSURE, TERSER, YUI
	}

	/**
	 * Display additional informational messages and warnings.
	 */
	@Parameter(property = "verbose", defaultValue = "false")
	protected boolean verbose;

	/**
	 * Display even more additional informational messages.
	 */
	@Parameter(property = "debug", defaultValue = "false")
	protected boolean debug;

	/**
	 * Display even more additional informational messages.
	 */
	@Parameter(property = "incrementalBuild", defaultValue = "true")
	protected boolean incrementalBuild;

	/**
	 * Size of the buffer used to read source files.
	 */
	@Parameter(property = "bufferSize", defaultValue = "4096")
	protected int bufferSize;

	/**
	 * If a supported character set is specified, it will be used to read the input file. Otherwise, it will assume that
	 * the platform's default character set is being used. The output file is encoded using the same character set.<br/>
	 * See the <a href="http://www.iana.org/assignments/character-sets">IANA Charset Registry</a> for a list of valid
	 * encoding types.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "charset", defaultValue = "${project.build.sourceEncoding}")
	protected String charset;

	/**
	 * The output file name suffix.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "suffix", defaultValue = "min")
	protected String suffix;

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
	protected boolean nosuffix;

	/**
	 * Skip the merge step. Minification will be applied to each source file individually.
	 *
	 * @since 1.5.2
	 */
	@Parameter(property = "skipMerge", defaultValue = "false")
	protected boolean skipMerge;

	/**
	 * Skip the minify step. Useful when merging files that are already minified.
	 *
	 * @since 1.5.2
	 */
	@Parameter(property = "skipMinify", defaultValue = "false")
	protected boolean skipMinify;

	/**
	 * Gzip target file into targetfile.gz
	 *
	 * @since 1.5.7
	 */
	@Parameter(property = "gzip", defaultValue = "false")
	protected boolean gzip;

	/**
	 * Zstandard compress target file into targetfile.zst
	 *
	 * @since 2.8.0-PATCHPUMP-R14
	 */
	@Parameter(property = "zstd", defaultValue = "false")
	protected boolean zstd;

	/**
	 * Webapp source directory.
	 */
	@Parameter(property = "webappSourceDir", defaultValue = "${basedir}/src/main/webapp")
	protected String webappSourceDir;

	/**
	 * Webapp target directory.
	 */
	@Parameter(property = "webappTargetDir", defaultValue = "${project.build.directory}/${project.build.finalName}")
	protected String webappTargetDir;

	/**
	 * Specify aggregations in an external JSON formatted config file.
	 *
	 * @since 1.7.5
	 */
	@Parameter(property = "bundleConfiguration")
	protected String bundleConfiguration;

	/**
	 * CSS source directory.
	 */
	@Parameter(property = "cssSourceDir", defaultValue = "")
	protected String cssSourceDir;

	/**
	 * CSS source include directory.
	 */
	@Parameter(property = "cssSourceIncludeDir", defaultValue = "")
	protected String cssSourceIncludeDir;

	/**
	 * CSS source file names list.
	 */
	@Parameter(property = "cssSourceFiles", alias = "cssFiles")
	protected ArrayList<String> cssSourceFiles;

	/**
	 * CSS files to include. Specified as fileset patterns which are relative to the CSS source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "cssSourceIncludes", alias = "cssIncludes")
	protected ArrayList<String> cssSourceIncludes;

	/**
	 * CSS files to exclude. Specified as fileset patterns which are relative to the CSS source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "cssSourceExcludes", alias = "cssExcludes")
	protected ArrayList<String> cssSourceExcludes;

	/**
	 * CSS target directory. Takes the same value as {@code cssSourceDir} when empty.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "cssTargetDir")
	protected String cssTargetDir;

	/**
	 * CSS output file name.
	 */
	@Parameter(property = "cssFinalFile", defaultValue = "style.css")
	protected String cssFinalFile;

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
	protected Engine cssEngine;

	/**
	 * JavaScript source directory.
	 */
	@Parameter(property = "jsSourceDir", defaultValue = "")
	protected String jsSourceDir;

	/**
	 * JavaScript source directory for jsSourceIncludes.
	 */
	@Parameter(property = "jsSourceIncludeDir", defaultValue = "")
	protected String jsSourceIncludeDir;

	/**
	 * JavaScript source file names list.
	 */
	@Parameter(property = "jsSourceFiles", alias = "jsFiles")
	protected ArrayList<String> jsSourceFiles;

	/**
	 * JavaScript files to include. Specified as fileset patterns which are relative to the JavaScript source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "jsSourceIncludes", alias = "jsIncludes")
	protected ArrayList<String> jsSourceIncludes;

	/**
	 * JavaScript files to exclude. Specified as fileset patterns which are relative to the JavaScript source directory.
	 *
	 * @since 1.2
	 */
	@Parameter(property = "jsSourceExcludes", alias = "jsExcludes")
	protected ArrayList<String> jsSourceExcludes;

	/**
	 * JavaScript target directory. Takes the same value as {@code jsSourceDir} when empty.
	 *
	 * @since 1.3.2
	 */
	@Parameter(property = "jsTargetDir")
	protected String jsTargetDir;

	/**
	 * JavaScript output file name.
	 */
	@Parameter(property = "jsFinalFile", defaultValue = "script.js")
	protected String jsFinalFile;

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
	@Parameter(property = "jsEngine", defaultValue = "CLOSURE")
	protected Engine jsEngine;

	/**
	 * Some source control tools don't like files containing lines longer than, say 8000 characters. The line-break
	 * option is used in that case to split long lines after a specific column. It can also be used to make the code
	 * more readable and easier to debug. Specify {@code 0} to get a line break after each semi-colon in JavaScript, and
	 * after each rule in CSS. Specify {@code -1} to disallow line breaks.
	 */
	@Parameter(property = "yuiLineBreak", defaultValue = "-1")
	protected int yuiLineBreak;

	/**
	 * Minify only. Do not obfuscate local symbols.
	 */
	@Parameter(property = "yuiNoMunge", defaultValue = "false")
	protected boolean yuiNoMunge;

	/**
	 * Preserve unnecessary semicolons (such as right before a '}'). This option is useful when compressed code has to
	 * be run through JSLint.
	 */
	@Parameter(property = "yuiPreserveSemicolons", defaultValue = "false")
	protected boolean yuiPreserveSemicolons;

	/**
	 * Disable all the built-in micro-optimizations.
	 */
	@Parameter(property = "yuiDisableOptimizations", defaultValue = "false")
	protected boolean yuiDisableOptimizations;

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
	protected LanguageMode closureLanguage;

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
	protected CompilationLevel closureCompilationLevel;

	/**
	 * @since patchpump-R5
	 */
	@Parameter(property = "closureWarningLevels")
	protected Map<String, String> closureWarningLevels;

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
	protected ArrayList<String> closureExterns;

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
	protected boolean closureUseDefaultExterns;

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
	protected boolean closureCreateSourceMap;

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
	protected boolean closureSortDependencies;

	/**
	 * Generate {@code $inject} properties for AngularJS for functions annotated with {@code @ngInject}.
	 *
	 * @since 1.7.3
	 */
	@Parameter(property = "closureAngularPass", defaultValue = "false")
	protected boolean closureAngularPass;
	
	protected void fillOptionalValues() {
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

	protected YuiConfig fillYuiConfig() {
		return new YuiConfig(yuiLineBreak, yuiNoMunge, yuiPreserveSemicolons, yuiDisableOptimizations);
	}

	protected ClosureConfig fillClosureConfig() {
		
		DependencyOptions dependencyOptions = closureSortDependencies ? DependencyOptions.sortOnly() : DependencyOptions.none();
		List<SourceFile> externs = new ArrayList<>();
		for (String extern : closureExterns)
			externs.add(SourceFile.fromFile(webappSourceDir + File.separator + extern, Charset.forName(charset)));

		return new ClosureConfig(closureLanguage, closureCompilationLevel, dependencyOptions, externs,
			closureWarningLevels, closureUseDefaultExterns, closureCreateSourceMap, closureAngularPass);
	}
}
