package cc.whohow.fs.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.util.Files;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

public class FileMetadata {
    protected final File metadata;

    public FileMetadata(File metadata) {
        this.metadata = metadata;
    }

    public File getFileMetadata() {
        return metadata;
    }

    public Optional<String> getString(String key) {
        return Files.optional(metadata.resolve(key))
                .map(File::readUtf8);
    }

    public Optional<Boolean> getBoolean(String key) {
        return getString(key)
                .map(Boolean::parseBoolean);
    }

    public Optional<Integer> getInteger(String key) {
        return getString(key)
                .map(Integer::parseInt);
    }

    public Optional<Long> getLong(String key) {
        return getString(key)
                .map(Long::parseLong);
    }

    public Optional<BigDecimal> getNumber(String key) {
        return getString(key)
                .map(BigDecimal::new);
    }

    public Optional<Duration> getDuration(String key) {
        return getString(key)
                .map(Duration::parse);
    }

    @Override
    public String toString() {
        return "FileMetadata(" + metadata.toString() + ")";
    }
}
