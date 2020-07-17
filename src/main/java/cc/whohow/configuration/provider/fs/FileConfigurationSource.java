package cc.whohow.configuration.provider.fs;

import cc.whohow.configuration.provider.AbstractConfiguration;
import cc.whohow.fs.File;
import cc.whohow.fs.FileEvent;
import cc.whohow.fs.FileListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

public class FileConfigurationSource extends AbstractConfiguration<ByteBuffer> implements FileListener {
    private static final Logger log = LogManager.getLogger(FileConfigurationSource.class);
    protected final File file;

    public FileConfigurationSource(File file) {
        this.file = file;
        this.file.watch(this);
    }

    @Override
    public ByteBuffer get() {
        return file.read();
    }

    @Override
    public void set(ByteBuffer value) {
        file.write(value);
    }

    @Override
    public synchronized void close() throws Exception {
        if (listeners.isEmpty()) {
            // 引用计数为0
            file.unwatch(this);
        } else {
            log.debug("close with listeners({})", listeners.size());
        }
    }

    @Override
    public void handleEvent(FileEvent event) throws Exception {
        switch (event.kind()) {
            case CREATE:
            case MODIFY: {
                notify(get());
                break;
            }
            case DELETE: {
                notify(null);
                break;
            }
            default: {
                break;
            }
        }
    }
}
