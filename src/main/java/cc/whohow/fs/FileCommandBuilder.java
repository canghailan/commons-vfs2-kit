package cc.whohow.fs;

import java.util.Optional;

@FunctionalInterface
public interface FileCommandBuilder<C extends FileCommand<?>> {
    Optional<? extends C> newCommand(String... arguments);
}
