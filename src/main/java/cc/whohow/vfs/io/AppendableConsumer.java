package cc.whohow.vfs.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class AppendableConsumer implements Consumer<String> {
    protected final Appendable appendable;
    protected final String prefix;
    protected final String suffix;

    public AppendableConsumer(Appendable appendable) {
        this(appendable, "", "");
    }

    public AppendableConsumer(Appendable appendable, String prefix, String suffix) {
        this.appendable = appendable;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public void accept(String s) {
        try {
            if (!prefix.isEmpty()) {
                appendable.append(prefix);
            }
            appendable.append(s);
            if (!suffix.isEmpty()) {
                appendable.append(suffix);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
