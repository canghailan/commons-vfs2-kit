package cc.whohow.fs.shell.script;

import cc.whohow.fs.shell.FileShell;
import groovy.lang.Closure;

import java.util.Objects;

public class FishCommand extends Closure<Object> {
    protected final FileShell fileShell;
    protected final String commandName;

    public FishCommand(FileShell fileShell, String commandName) {
        super(fileShell, fileShell);
        this.fileShell = fileShell;
        this.commandName = commandName;
    }

    @Override
    public Object call(Object... args) {
        String[] commandArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            commandArgs[i] = Objects.toString(args[i], null);
        }
        return fileShell.exec(commandName, commandArgs);
    }

    @Override
    public String toString() {
        return commandName;
    }
}
