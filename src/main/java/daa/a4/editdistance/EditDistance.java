package daa.a4.editdistance;

import daa.a4.metrics.Counters;

/**
 * Edit distance: the smallest number of insertions, deletions and replacements
 * that turn string a into string b.
 *
 * All three versions share this one signature, so the experiment is one loop.
 */
public interface EditDistance {

    /** Short name used in results.csv and in the plots. */
    String name();

    /** Returns the edit distance between a and b. Empty strings are allowed; null is not. */
    int distance(String a, String b);

    /** Counters of the last call(s). Call {@code counters().reset()} before a measured run. */
    Counters counters();

    static void requireNonNull(String a, String b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
    }
}
