package com.samaxes.maven.minify.plugin;

import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.samaxes.maven.minify.common.ClosureConfig;
import com.samaxes.maven.minify.common.YuiConfig;
import com.samaxes.maven.minify.plugin.AbstractMinifyMojo.Engine;

/**
 * Task options with horrible constructors.
 */
public class TaskOptions {

	public enum Type {
		CSS, JS
	}

	final Log log;
	final boolean verbose;
	final boolean debug;
	final boolean incrementalBuild;
	final Integer bufferSize;
	final String charset;
	final String suffix;
	final boolean nosuffix;

	final boolean skipMerge;
	final boolean skipMinify;
	final Engine engine;
	final YuiConfig yuiConfig;
	final String sourceDir;
	final String sourceIncludeDir;
	final String targetDir;
	final String mergedFilename;
	final String webappSourceDir;
	final String webappTargetDir;
	final List<String> sourceFiles;
	final List<String> sourceIncludes;
	final List<String> sourceExcludes;
	final boolean gzip;
	final boolean zstd;
	final Type type;
	ClosureConfig closureConfig;

	private TaskOptions(Log log, boolean verbose, boolean debug, boolean incrementalBuild, Integer bufferSize, String charset, String suffix, boolean nosuffix,
		boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir, String sourceDir,
		String sourceIncludeDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
		String targetDir, String mergedFilename, Engine engine, YuiConfig yuiConfig, boolean gzip, boolean zstd,
		ClosureConfig closureConfig, Type type) {

		this.log = log;
		this.verbose = verbose;
		this.debug = debug;
		this.incrementalBuild = incrementalBuild;
		this.bufferSize = bufferSize;
		this.charset = charset;
		this.suffix = suffix;
		this.nosuffix = nosuffix;
		this.skipMerge = skipMerge;
		this.skipMinify = skipMinify;
		this.sourceDir = sourceDir;
		this.sourceIncludeDir = sourceIncludeDir;
		this.sourceFiles = sourceFiles;
		this.sourceIncludes = sourceIncludes;
		this.sourceExcludes = sourceExcludes;
		this.targetDir = targetDir;
		this.mergedFilename = mergedFilename;
		this.webappSourceDir = webappSourceDir;
		this.webappTargetDir = webappTargetDir;
		this.engine = engine;
		this.yuiConfig = yuiConfig;
		this.gzip = gzip;
		this.zstd = zstd;
		this.type = type;
		this.closureConfig = closureConfig;
	}

	/**
	 * CSS task options
	 */
	public TaskOptions(Log log, boolean verbose, boolean debug, boolean incrementalBuild, Integer bufferSize, String charset, String suffix, boolean nosuffix,
		boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir, String cssSourceDir,
		String cssSourceIncludeDir, List<String> cssSourceFiles, List<String> cssSourceIncludes,
		List<String> cssSourceExcludes, String cssTargetDir, String cssFinalFile, Engine cssEngine, YuiConfig yuiConfig,
		boolean gzip, boolean zstd) {

		this(log, verbose, debug, incrementalBuild, bufferSize, charset, suffix, nosuffix, skipMerge, skipMinify, webappSourceDir,
			webappTargetDir, cssSourceDir, cssSourceIncludeDir, cssSourceFiles, cssSourceIncludes, cssSourceExcludes,
			cssTargetDir, cssFinalFile, cssEngine, yuiConfig, gzip, zstd, null, Type.CSS);
	}

	/**
	 * JS task options.
	 */
	public TaskOptions(Log log, boolean verbose, boolean debug, boolean incrementalBuild, int bufferSize, String charset, String suffix, boolean nosuffix,
		boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir, String jsSourceDir,
		String jsSourceIncludeDir, List<String> jsSourceFiles, List<String> jsSourceIncludes,
		List<String> jsSourceExcludes, String jsTargetDir, String jsFinalFile, Engine jsEngine, YuiConfig yuiConfig,
		ClosureConfig closureConfig, boolean gzip, boolean zstd) {

		this(log, verbose, debug, incrementalBuild, bufferSize, charset, suffix, nosuffix, skipMerge, skipMinify, webappSourceDir,
			webappTargetDir, jsSourceDir, jsSourceIncludeDir, jsSourceFiles, jsSourceIncludes, jsSourceExcludes,
			jsTargetDir, jsFinalFile, jsEngine, yuiConfig, gzip, zstd, closureConfig, Type.JS);
	}

	@Override
	public String toString() {
		return "TaskOptions [verbose=" + verbose + ", debug=" + debug + ", bufferSize=" + bufferSize + ", charset="
			+ charset + ", suffix=" + suffix + ", nosuffix=" + nosuffix + ", skipMerge=" + skipMerge + ", skipMinify="
			+ skipMinify + ", engine=" + engine + ", yuiConfig=" + yuiConfig + ", sourceDir=" + sourceDir
			+ ", sourceIncludeDir=" + sourceIncludeDir + ", targetDir=" + targetDir + ", mergedFilename="
			+ mergedFilename + ", webappSourceDir=" + webappSourceDir + ", webappTargetDir=" + webappTargetDir
			+ ", sourceFiles=" + sourceFiles + ", sourceIncludes=" + sourceIncludes + ", sourceExcludes="
			+ sourceExcludes + ", gzip=" + gzip + ", zstd=" + zstd + ", type=" + type + ", closureConfig=" + closureConfig + "]";
	}
}
