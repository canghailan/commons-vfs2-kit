package cc.whohow.fs.command;

import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.command.provider.Checksum;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

/**
 * 将有(VirtualFileSystem, String...)构造器的Callable对象适配为命令
 *
 * @see Checksum#Checksum(cc.whohow.fs.VirtualFileSystem, java.lang.String...)
 */
public class CallableCommand<R> implements BiFunction<VirtualFileSystem, String[], R> {
    protected final Class<? extends Callable<R>> command;
    protected final Constructor<? extends Callable<R>> constructor;

    public CallableCommand(Class<? extends Callable<R>> command) throws Exception {
        this.command = command;
        this.constructor = command.getConstructor(VirtualFileSystem.class, String[].class);
    }

    public static <R, C extends Callable<R>> C build(Class<C> command, VirtualFileSystem vfs, String... args) {
        try {
            return command.getConstructor(VirtualFileSystem.class, String[].class).newInstance(vfs, args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public String getCommandName() {
        return command.getName();
    }

    public String getShortCommandName() {
        return command.getSimpleName();
    }

    @Override
    public R apply(VirtualFileSystem vfs, String[] args) {
        try {
            return constructor.newInstance(vfs, args).call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }
}
