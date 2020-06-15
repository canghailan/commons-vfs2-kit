package cc.whohow.fs.util;

import java.net.URI;
import java.util.Objects;

public class Paths {
    public static String getName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return new PathParser(path).getLastName();
    }

    public static int getNameCount(String path) {
        return 0;
    }

    public static String getParent(String path) {
        int index = path.lastIndexOf('/');
        if (index == path.length() - 1) {
            index = path.lastIndexOf('/', index);
        }
        if (index < 0) {
            return null;
        } else {
            return path.substring(0, index + 1);
        }
    }

    public static String getExtension(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        int index = name.lastIndexOf('.');
        // index == 0 不是扩展名
        if (0 < index && index < name.length() - 1) {
            return name.substring(index + 1);
        }
        return "";
    }

    public static boolean startsWith(URI thisUri, URI thatUri) {
        return Objects.equals(thatUri.getScheme(), thisUri.getScheme()) &&
                Objects.equals(thatUri.getHost(), thisUri.getHost()) &&
                thatUri.getPort() == thisUri.getPort() &&
                (thisUri.getPath() == null ||
                        (thatUri.getPath() != null && thisUri.getPath().startsWith(thatUri.getPath())));
    }

    public static String relativize(URI thisUri, URI thatUri) {
        URI relative = thisUri.relativize(thatUri);
        if (relative.isAbsolute() || relative.toString().startsWith("/")) {
            throw new IllegalArgumentException(thatUri.toString());
        }
        return relative.toString();
    }
}
