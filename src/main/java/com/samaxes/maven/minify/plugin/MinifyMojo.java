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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.samaxes.maven.minify.common.Aggregation;
import com.samaxes.maven.minify.common.AggregationConfiguration;
import com.samaxes.maven.minify.common.ClosureConfig;
import com.samaxes.maven.minify.common.YuiConfig;

/**
 * Goal for combining and minifying CSS and JavaScript files.
 */
@Mojo(name = "minify", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class MinifyMojo extends AbstractMinifyMojo {

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
