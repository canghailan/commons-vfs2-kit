package cc.whohow.fs.util;

import java.net.URI;
import java.util.Objects;

public class Paths {
    public static String getName(String path) {
        return new PathParser(path).getLastName();
    }

    public static int getNameCount(String path) {
        return new PathParser(path).getNameCount();
    }

    public static String getParent(String path) {
        return new PathParser(path).getParent();
    }

    public static String getExtension(String name) {
        return PathParser.getExtension(name);
    }

    public static String getName(URI uri) {
        return getName(uri.getPath());
    }

    public static URI getParent(URI uri) {
        String parent = getParent(uri.getPath());
        if (parent == null) {
            return null;
        }
        return new UriBuilder(uri)
                .setPath(parent)
                .setQuery(null)
                .setFragment(null)
                .build();
    }

    public static String getExtension(URI uri) {
        return getExtension(getName(uri));
    }

    public static boolean startsWith(String thisPath, String thatPath) {
        if (thatPath == null) {
            return true;
        }
        if (thisPath == null) {
            return false;
        }
        return thisPath.startsWith(thatPath);
    }

    public static boolean startsWith(URI thisUri, URI thatUri) {
        return startsWith(thisUri.getPath(), thatUri.getPath()) &&
                Objects.equals(thatUri.getScheme(), thisUri.getScheme()) &&
                Objects.equals(thatUri.getHost(), thisUri.getHost()) &&
                thatUri.getPort() == thisUri.getPort();
    }

    public static String relativize(URI thisUri, URI thatUri) {
        URI relative = thisUri.relativize(thatUri);
        if (relative.getScheme() != null ||
                relative.getUserInfo() != null ||
                relative.getHost() != null ||
                relative.getQuery() != null ||
                relative.getFragment() != null ||
                relative.getPath() == null ||
                relative.getPath().startsWith("/")) {
            throw new IllegalArgumentException(thatUri.toString());
        }
        return relative.getPath();
    }
}
