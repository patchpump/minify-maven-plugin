package com.samaxes.maven.minify.common;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;

/**
 * UglifyJS 3 compiler.
 * 
 * @author patchpump
 */
public class UglifyJS3Compiler extends AbstractJavaScriptCompiler {

	public UglifyJS3Compiler() throws IOException, ScriptException {
		super("/META-INF/uglifyjs/uglifyjs-min-3.4.9.js");
	}
	
	@Override
	public String compile(InputStreamReader reader) throws IOException, ScriptException, NoSuchMethodException {
		String source = IOUtils.toString(reader);
		synchronized(script) {
			ScriptEngine engine = script.getEngine();
			Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.put("input", source);
			return (String)engine.eval("uglifysmth()", bindings);
		}
	}
}
