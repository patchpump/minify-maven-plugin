package com.samaxes.maven.minify.common;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Uses the JavaScript ScriptEngine still present in JDK 10 to run compilers.
 * 
 * NOTE: not thread-safe as compiler source is only evaluated once and bindings is re-used
 * 
 * @author patchpump
 */
public abstract class AbstractJavaScriptCompiler {

	private static final ScriptEngine scriptEngine = new ScriptEngineManager(null).getEngineByName("JavaScript");
	private static final Map<String,CompiledScript> scriptCache = new ConcurrentHashMap<>();

	protected final CompiledScript script;
	
	public AbstractJavaScriptCompiler(String engineFile) throws IOException, ScriptException {

		CompiledScript compiled = scriptCache.get(engineFile);
		if (compiled != null) {
			script = compiled;
			return;
		}

		System.out.println("Creating compiler..");
		try (InputStreamReader inputStreamReader = new InputStreamReader(AbstractJavaScriptCompiler.class.getResourceAsStream(engineFile), StandardCharsets.UTF_8)) {
			compiled = ((Compilable)scriptEngine).compile(inputStreamReader);
			Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			compiled.eval(bindings);
			CompiledScript cached = scriptCache.putIfAbsent(engineFile, compiled);
			script = cached != null ? cached : compiled;
		}
		System.out.println("Created compiler..");
	}

	protected abstract String compile(InputStreamReader reader) throws IOException, ScriptException, NoSuchMethodException;

	public void compile(InputStreamReader reader, OutputStreamWriter writer, JavaScriptErrorReporter javaScriptErrorReporter) throws NoSuchMethodException, IOException, ScriptException {
		writer.write(compile(reader));
	}
}
