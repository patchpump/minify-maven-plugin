package patchpump.minify.maven.plugin;

import java.util.List;

import org.apache.maven.plugin.logging.Log;

import patchpump.minify.maven.common.ClosureConfig;
import patchpump.minify.maven.common.YuiConfig;
import patchpump.minify.maven.plugin.AbstractMinifyMojo.Engine;

/**
 * Task options with horrible constructors.
 */
public class TaskOptions {

	public enum Type {
		CSS, JS
	}

	final ClosureConfig closureConfig;
	final Engine engine;
	final Log log;
	final Type type;
	final YuiConfig yuiConfig;
	
	final List<String> sourceExcludes;
	final List<String> sourceFiles;
	final List<String> sourceIncludes;
	
	final String charset;
	final String mergedFilename;
	final String sourceDir;
	final String sourceIncludeDir;
	final String suffix;
	final String targetDir;
	final String webappSourceDir;
	final String webappTargetDir;
	final String zstdDirectoryDir;

	final boolean debug;
	final boolean gzip;
	final boolean incrementalBuild;
	final boolean nosuffix;
	final boolean skipMerge;
	final boolean skipMinify;
	final boolean verbose;
	
	final int bufferSize;	
	final int zstd;

	private TaskOptions(Log log, boolean verbose, boolean debug, boolean incrementalBuild, int bufferSize, String charset, String suffix, boolean nosuffix,
		boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir, String zstdDirectoryDir, String sourceDir,
		String sourceIncludeDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
		String targetDir, String mergedFilename, Engine engine, YuiConfig yuiConfig, boolean gzip, int zstd,
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
		this.webappSourceDir = webappSourceDir;
		this.webappTargetDir = webappTargetDir;
		this.zstdDirectoryDir = zstdDirectoryDir;
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;
		this.sourceIncludeDir = sourceIncludeDir;
		this.sourceFiles = sourceFiles;
		this.sourceIncludes = sourceIncludes;
		this.sourceExcludes = sourceExcludes;
		this.mergedFilename = mergedFilename;
		this.engine = engine;
		this.yuiConfig = yuiConfig;
		this.gzip = gzip;
		this.zstd = zstd;
		this.closureConfig = closureConfig;
		this.type = type;
	}

	/**
	 * CSS task options
	 */
	public TaskOptions(Log log, boolean verbose, boolean debug, boolean incrementalBuild, int bufferSize, String charset, String suffix, boolean nosuffix,
		boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir, String zstdDirectoryDir, String cssSourceDir,
		String cssSourceIncludeDir, List<String> cssSourceFiles, List<String> cssSourceIncludes,
		List<String> cssSourceExcludes, String cssTargetDir, String cssFinalFile, Engine cssEngine, YuiConfig yuiConfig,
		boolean gzip, int zstd) {

		this(log, verbose, debug, incrementalBuild, bufferSize, charset, suffix, nosuffix, skipMerge, skipMinify, webappSourceDir,
			webappTargetDir, zstdDirectoryDir, cssSourceDir, cssSourceIncludeDir, cssSourceFiles, cssSourceIncludes, cssSourceExcludes,
			cssTargetDir, cssFinalFile, cssEngine, yuiConfig, gzip, zstd, null, Type.CSS);
	}

	/**
	 * JS task options.
	 */
	public TaskOptions(Log log, boolean verbose, boolean debug, boolean incrementalBuild, int bufferSize, String charset, String suffix, boolean nosuffix,
		boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir, String zstdDirectoryDir, String jsSourceDir,
		String jsSourceIncludeDir, List<String> jsSourceFiles, List<String> jsSourceIncludes,
		List<String> jsSourceExcludes, String jsTargetDir, String jsFinalFile, Engine jsEngine, YuiConfig yuiConfig,
		ClosureConfig closureConfig, boolean gzip, int zstd) {

		this(log, verbose, debug, incrementalBuild, bufferSize, charset, suffix, nosuffix, skipMerge, skipMinify, webappSourceDir,
			webappTargetDir, zstdDirectoryDir, jsSourceDir, jsSourceIncludeDir, jsSourceFiles, jsSourceIncludes, jsSourceExcludes,
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
			+ sourceExcludes + ", gzip=" + gzip + ", zstd=" + zstd + ", type=" + type + ", closureConfig=" + closureConfig
			+ ", zstdDirectoryDir=" + zstdDirectoryDir + "]";
	}
}
