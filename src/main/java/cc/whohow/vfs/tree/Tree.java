package cc.whohow.vfs.tree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Tree<T> implements Iterable<T> {
    protected final T root;
    protected final Function<T, ? extends Stream<? extends T>> getChildren;
    protected final BiFunction<Function<T, ? extends Stream<? extends T>>, T, Iterator<T>> traverse;

    public Tree(T root,
                Function<T, ? extends Stream<? extends T>> getChildren,
                BiFunction<Function<T, ? extends Stream<? extends T>>, T, Iterator<T>> traverse) {
        this.root = root;
        this.getChildren = getChildren;
        this.traverse = traverse;
    }

    public Tree(T root,
                Predicate<T> hasChildren,
                Function<T, T[]> getChildren,
                BiFunction<Function<T, ? extends Stream<? extends T>>, T, Iterator<T>> traverse) {
        this(root, new GetChildrenAdapter<>(hasChildren, getChildren), traverse);
    }

    @Override
    public Iterator<T> iterator() {
        return traverse.apply(getChildren, root);
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private static final class GetChildrenAdapter<T> implements Function<T, Stream<T>> {
        private Predicate<T> hasChildren;
        private Function<T, T[]> getChildren;

        public GetChildrenAdapter(Predicate<T> hasChildren, Function<T, T[]> getChildren) {
            this.hasChildren = hasChildren;
            this.getChildren = getChildren;
        }

        @Override
        public Stream<T> apply(T node) {
            if (hasChildren.test(node)) {
                return Arrays.stream(getChildren.apply(node));
            }
            return null;
        }
    }
}
