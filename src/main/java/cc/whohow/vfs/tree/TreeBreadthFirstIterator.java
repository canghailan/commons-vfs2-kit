package cc.whohow.vfs.tree;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.Function;

/**
 * 广度优先，先序遍历
 */
public class TreeBreadthFirstIterator<T> implements Iterator<T> {
    private final Function<T, ? extends Iterator<? extends T>> getChildren;
    private final T root;
    private final Queue<T> queue = new ArrayDeque<>();

    public TreeBreadthFirstIterator(Function<T, ? extends Iterator<? extends T>> getChildren, T root) {
        this.getChildren = getChildren;
        this.root = root;
        this.queue.offer(root);
    }

    public T getRoot() {
        return root;
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public T next() {
        T head = queue.poll();
        Iterator<? extends T> children = getChildren.apply(head);
        if (children != null) {
            while (children.hasNext()) {
                queue.offer(children.next());
            }
        }
        return head;
    }
}
