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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package patchpump.minify.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import patchpump.minify.maven.common.SafeCssCompressor;
import patchpump.minify.maven.common.YuiCssCompressor;

/**
 * Task for merging and compressing CSS files.
 */
public class ProcessCSSFilesTask extends ProcessFilesTask {

	/**
	 * Task constructor.
	 *
	 * @throws FileNotFoundException when the given source file does not exist
	 */
	public ProcessCSSFilesTask(final TaskOptions opt) throws FileNotFoundException {
		super(opt);
	}

	/**
	 * Minifies a CSS file. Create missing parent directories if needed.
	 *
	 * @param mergedFile input file resulting from the merged step
	 * @param minifiedFile output file resulting from the minify step
	 * @throws IOException when the minify step fails
	 */
	@Override
	protected void minify(File mergedFile, File minifiedFile) throws IOException {

		minifiedFile.getParentFile().mkdirs();
		File compressedFile = new File(minifiedFile.getAbsolutePath() + ".gz");
		File zstdFile = new File(minifiedFile.getAbsolutePath() + ".zst");
		File dczFile = new File(minifiedFile.getAbsolutePath() + ".dcz");
		OutputStream out = new FileOutputStream(minifiedFile);
		try (InputStream in = new FileInputStream(mergedFile);
			InputStreamReader reader = new InputStreamReader(in, opt.charset);
			OutputStreamWriter writer = new OutputStreamWriter(out, opt.charset)) {

			log.info("Creating minified file [" + ((opt.verbose) ? minifiedFile.getPath() : minifiedFile.getName()) + "].");

			switch (opt.engine) {
			case YUI:
				log.debug("Using YUI Compressor engine.");
				YuiCssCompressor compressor = new YuiCssCompressor(reader);
				compressor.compress(writer, opt.yuiConfig.getLineBreak());
				break;

			case SAFECSS:
				log.debug("Using SAFECSS Compressor engine.");
				new SafeCssCompressor(reader).compress(writer);
				break;

			default:
				log.warn("CSS engine not supported.");
				break;
			}
		
			writer.flush();

		} catch (Exception e) {
			close(out);
			minifiedFile.delete();
			compressedFile.delete();
			dczFile.delete();
			log.error("Failed to compress the CSS file [" + ((opt.verbose) ? mergedFile.getPath() : mergedFile.getName()) + "].", e);
			throw new IOException(e);

		} finally {
			close(out);
		}

		if (opt.gzip)
			gzip(minifiedFile, compressedFile);

		if (opt.zstd > 0) {
			zstd(minifiedFile, zstdFile, opt.zstd);
			if (opt.zstdDirectoryDir != null)
				zstd(minifiedFile, dczFile, opt.zstdDirectoryDir, opt.zstd);
		}
	}
}
