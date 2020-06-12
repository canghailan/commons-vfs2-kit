package cc.whohow.fs.util;

import java.util.function.Function;
import java.util.function.Predicate;

public class MappingPredicate<T, R> implements Predicate<T> {
    private final Predicate<R> predicate;
    private final Function<T, R> function;

    public MappingPredicate(Predicate<R> predicate, Function<T, R> function) {
        this.predicate = predicate;
        this.function = function;
    }

    @Override
    public boolean test(T t) {
        return predicate.test(function.apply(t));
    }
}
