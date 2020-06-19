package cc.whohow.fs.command.script;

import groovy.lang.Closure;

import java.util.Objects;
import java.util.function.Function;

public class FishCommand extends Closure<Object> {
    protected final Function<String[], ?> command;

    public FishCommand(Function<String[], ?> command) {
        super(command, command);
        this.command = command;
    }

    @Override
    public Object call(Object... args) {
        String[] stringArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            stringArgs[i] = Objects.toString(args[i], null);
        }
        return command.apply(stringArgs);
    }
}
