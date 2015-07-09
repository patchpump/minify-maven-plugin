package com.samaxes.maven.minify.common;

import org.apache.commons.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

/**
 * Based on DustCompiler from https://github.com/patchpump/dustjs-maven-plugin
 */
public class UglifyJS2Compiler {
	
	private ScriptEngineManager mgr = new ScriptEngineManager();
	private ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
	
	Invocable invocable;

	String dustFilename = "META-INF/uglifyjs/uglifyjs-min-2.4.23.js";

	public UglifyJS2Compiler() throws IOException, ScriptException {

		ClassLoader loader = getClass().getClassLoader();
		URL resource = loader.getResource(dustFilename);

		try (InputStreamReader inputStreamReader = new InputStreamReader(resource.openConnection().getInputStream())) {
			jsEngine.eval(inputStreamReader);
			invocable = (Invocable)jsEngine;
		}
	}

	public String compile(InputStreamReader reader) throws IOException, ScriptException, NoSuchMethodException {

		String source = IOUtils.toString(reader);
		Object scopeObject = getScopeObjectFromEngine();
		Object[] params = new Object[] { source };
		return (String)invocable.invokeMethod(scopeObject, "uglify", params);
	}

	private Object getScopeObjectFromEngine() {
		return jsEngine.get("uglifier");
	}

	public void compile(InputStreamReader reader, OutputStreamWriter writer, JavaScriptErrorReporter javaScriptErrorReporter) throws NoSuchMethodException, IOException, ScriptException {
		writer.write(compile(reader));
	}
}
