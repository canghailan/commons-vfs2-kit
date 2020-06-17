package cc.whohow.fs;

public interface Copy<F1, F2> extends Command<F2> {
    F1 getSource();

    F2 getTarget();
}
