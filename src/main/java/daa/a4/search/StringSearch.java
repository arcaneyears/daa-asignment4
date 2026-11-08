package daa.a4.search;

import daa.a4.metrics.Counters;

import java.util.List;

/**
 * Finds every occurrence of a pattern in a text.
 * Returns start indices (from 0) in increasing order, overlapping matches included.
 */
public interface StringSearch {

    String name();

    /**
     * @throws IllegalArgumentException if the pattern is empty (or text/pattern is null)
     * @return all start positions; an empty list if there is no match or the pattern is longer than the text
     */
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
