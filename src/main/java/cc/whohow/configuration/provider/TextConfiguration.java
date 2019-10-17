package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.serialize.TextSerializer;

import java.nio.charset.Charset;

public class TextConfiguration extends FileValue.Cache<String> implements Configuration<String> {
    public TextConfiguration(CloudFileObject fileObject) {
        super(fileObject, TextSerializer.utf8());
    }

    public TextConfiguration(CloudFileObject fileObject, Charset charset) {
        super(fileObject, new TextSerializer(charset));
    }
}
