package cc.whohow.vfs.tree;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 树遍历器，深度优先，后序遍历
 */
public class TreePostOrderIterator<T> implements Iterator<T> {
    private final Function<T, ? extends Stream<? extends T>> getChildren;
    private final T root;
    private final Deque<Queue<T>> stack = new ArrayDeque<>();

    public TreePostOrderIterator(Function<T, ? extends Stream<? extends T>> getChildren, T root) {
        this.getChildren = getChildren;
        this.root = root;
        this.stack.push(new ArrayDeque<>(Collections.singleton(root)));
    }

    public T getRoot() {
        return root;
    }

    @Override
    public boolean hasNext() {
        if (stack.size() > 1) {
            return true;
        }
        Queue<T> top = stack.peek();
        return top != null && !top.isEmpty();
    }

    @Override
    public T next() {
        push();
        return pop();
    }

    private void push() {
        while (true) {
            Queue<T> top = stack.peek();
            if (top == null) {
                return;
            }

            T left = top.peek();
            if (left == null) {
                return;
            }

            Queue<T> newTop = new ArrayDeque<>();
            try (Stream<? extends T> children = getChildren.apply(left)) {
                if (children != null) {
                    children.forEach(newTop::offer);
                } else {
                    return;
                }
            }
            if (newTop.isEmpty()) {
                return;
            }

            stack.push(newTop);
        }
    }

    private T pop() {
        while (true) {
            Queue<T> top = stack.peek();
            if (top == null) {
                return null;
            }

            T left = top.poll();
            if (left == null) {
                stack.pop();
                continue;
            }

            return left;
        }
    }
}