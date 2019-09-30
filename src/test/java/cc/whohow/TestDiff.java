package cc.whohow;

import cc.whohow.vfs.diff.MapDiffList;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

public class TestDiff {
    @Test
    public void test() {
        Map<String, String> a = new TreeMap<>();
        a.put("a", "a");
        a.put("b", "b");
        a.put("c", "c");

        Map<String, String> b = new TreeMap<>();
        b.put("a", "a1");
        b.put("b", "b");
        b.put("d", "d");

        new MapDiffList<>(a.entrySet(), b.entrySet()).forEach(System.out::println);
    }
}
