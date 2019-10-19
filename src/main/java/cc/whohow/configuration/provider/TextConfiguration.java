package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.serialize.FileValue;
import cc.whohow.vfs.serialize.TextSerializer;

import java.nio.charset.Charset;

public class TextConfiguration extends FileValue.Cache<String> implements Configuration<String> {
    public TextConfiguration(FileObjectX fileObject) {
        super(fileObject, TextSerializer.utf8());
    }

    public TextConfiguration(FileObjectX fileObject, Charset charset) {
        super(fileObject, new TextSerializer(charset));
    }
}
