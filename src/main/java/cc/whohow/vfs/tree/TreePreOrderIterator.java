package cc.whohow.vfs.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

/**
 * 树遍历器，深度优先，先序遍历
 */
public class TreePreOrderIterator<T> implements Iterator<T> {
    private final Function<T, ? extends Iterator<? extends T>> getChildren;
    private final T root;
    private final ArrayList<T> stack = new ArrayList<>();

    public TreePreOrderIterator(Function<T, ? extends Iterator<? extends T>> getChildren, T root) {
        this.getChildren = getChildren;
        this.root = root;
        this.stack.add(root);
    }

    public T getRoot() {
        return root;
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public T next() {
        T top = stack.remove(stack.size() - 1);
        Iterator<? extends T> children = getChildren.apply(top);
        if (children != null) {
            int from = stack.size();
            while (children.hasNext()) {
                stack.add(children.next());
            }
            int to = stack.size();
            for (int i = from, j = to - 1; i < j; i++, j--) {
                T temp = stack.get(i);
                stack.set(i, stack.get(j));
                stack.set(j, temp);
            }
        }
        return top;
    }
}