package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.Path;

import java.util.List;
import java.util.function.Consumer;

public class PollingFileWatchKey<P extends Path, F extends File<P, F>> implements Consumer<FileWatchEvent<P, F>> {
    private F watchable;
    private List<Consumer<FileWatchEvent<P, F>>> listeners;

    public void notify(FileWatchEvent<P, F> event) {
        if (watchable.equals(event.watchable())) {

        }
    }

    protected void notifyAll(FileWatchEvent<P, F> event) {
        for (Consumer<FileWatchEvent<P, F>> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void accept(FileWatchEvent<P, F> event) {

    }
}
