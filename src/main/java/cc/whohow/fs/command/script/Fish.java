package cc.whohow.fs.command.script;

import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.command.FileShell;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class Fish {
    protected final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("groovy");
    protected final FileShell fish;

    public Fish(FileShell fish) {
        this.fish = fish;
        this.fish.install("install", this::newInstaller);
        this.scriptEngine.setBindings(new FishContext(fish), ScriptContext.GLOBAL_SCOPE);
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

    protected Callable<?> newInstaller(VirtualFileSystem vfs, String... args) {
        return new Installer(fish, Arrays.asList(args));
    }

    private static class Installer implements Callable<List<String>> {
        protected final FileShell fish;
        protected final List<String> commands;

        public Installer(FileShell fish, List<String> commands) {
            this.fish = fish;
            this.commands = commands;
        }

        @Override
        public List<String> call() {
            commands.forEach(fish::install);
            return commands;
        }
    }
}
