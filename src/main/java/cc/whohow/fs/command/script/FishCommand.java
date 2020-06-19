package cc.whohow.fs.command.script;

import cc.whohow.fs.command.FileShell;
import groovy.lang.Closure;

import java.util.Objects;

public class FishCommand extends Closure<Object> {
    protected final FileShell fish;
    protected final String name;

    public FishCommand(FileShell fish, String name) {
        super(fish, fish);
        this.fish = fish;
        this.name = name;
    }

    @Override
    public Object call(Object... args) {
        String[] commandArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            commandArgs[i] = Objects.toString(args[i], null);
        }
        return fish.exec(name, commandArgs);
    }
}
