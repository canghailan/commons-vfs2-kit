package cc.whohow.configuration.provider;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.TextType;

import java.nio.charset.Charset;

public class TextConfiguration extends FileValue.Cache<String> {
    public TextConfiguration(FileObject fileObject) {
        super(fileObject, TextType.utf8());
    }

    public TextConfiguration(FileObject fileObject, Charset charset) {
        super(fileObject, new TextType(charset));
    }
}
