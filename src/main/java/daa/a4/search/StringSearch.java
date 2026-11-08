package daa.a4.search;

import daa.a4.metrics.Counters;

import java.util.List;

public interface StringSearch {
    String name();

    List<Integer> search(String text, String pattern);

    Counters counters();

    static void validate(String text, String pattern) {
        if (text == null || pattern == null) {
            throw new IllegalArgumentException("Text and pattern must not be null");
        }
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern must not be empty");
        }
    }
}
