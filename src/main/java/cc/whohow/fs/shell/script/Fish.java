package cc.whohow.fs.shell.script;

import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.shell.FileShell;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Fish {
    protected final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("groovy");

    public Fish(FileShell fileShell) {
        this.scriptEngine.setBindings(new FishContext(fileShell), ScriptContext.GLOBAL_SCOPE);
    }

    public Object get(String key) {
        return scriptEngine.get(key);
    }

    public void put(String key, String value) {
        scriptEngine.put(key, value);
    }

    public Object eval(String script) {
        try {
            return scriptEngine.eval(script);
        } catch (ScriptException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public Object eval(URL script) {
        try (Reader reader = new InputStreamReader(script.openStream(), StandardCharsets.UTF_8)) {
            return scriptEngine.eval(reader);
        } catch (IOException | ScriptException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public Object eval(Reader script) {
        try {
            return scriptEngine.eval(script);
        } catch (ScriptException e) {
            throw UncheckedException.unchecked(e);
        }
    }
}
