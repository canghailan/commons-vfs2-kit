package cc.whohow.fs;

import java.net.URI;
import java.util.Optional;

@FunctionalInterface
public interface FileResolver<P extends Path, F extends File<P, F>> {
    Optional<? extends File<P, F>> resolve(URI uri, CharSequence mountPoint, CharSequence path);
}
