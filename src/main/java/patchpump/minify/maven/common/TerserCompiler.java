package patchpump.minify.maven.common;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;

/**
 * terser compiler.
 * 
 * @author patchpump
 */
public class TerserCompiler extends AbstractJavaScriptCompiler {

	public TerserCompiler() throws IOException, ScriptException {
		super("/META-INF/terser/bundle.min.js");
	}
	
	@Override
	public String compile(InputStreamReader reader) throws IOException, ScriptException, NoSuchMethodException {
		String source = IOUtils.toString(reader);
		synchronized(script) {
			ScriptEngine engine = script.getEngine();
			Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.put("input", source);
			return (String)engine.eval("minifymth()", bindings);
		}
	}
}
