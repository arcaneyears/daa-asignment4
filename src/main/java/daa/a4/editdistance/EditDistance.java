package daa.a4.editdistance;

import daa.a4.metrics.Counters;

public interface EditDistance {
    String name();

    int distance(String a, String b);

    Counters counters();

    static void requireNonNull(String a, String b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
    }
}
