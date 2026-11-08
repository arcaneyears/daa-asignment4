package daa.a4.search;

import daa.a4.metrics.Counters;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt search. Time O(n + m), extra memory O(m) for the LPS array.
 *
 * lps[q] = length of the longest proper prefix of p[0..q] that is also a suffix of p[0..q].
 * After a mismatch (or a full match) the pattern pointer jumps back to lps[j-1],
 * so characters of the text that already matched are never checked again,
 * and the text index i never moves backwards.
 */
public final class KmpSearch implements StringSearch {

    private final Counters counters = new Counters();

    @Override
    public String name() {
        return "KMP";
    }

    /** Prefix function (LPS array) of the pattern. Public so that tests can call it. */
    public int[] lps(String p) {
        if (p == null) throw new IllegalArgumentException("Pattern must not be null");
        int m = p.length();
        int[] lps = new int[m];
        counters.memoryCells += m;
        int len = 0;              // length of the current matched border
        int q = 1;
        while (q < m) {
            counters.preprocessComparisons++;
            if (p.charAt(q) == p.charAt(len)) {
                len++;
                lps[q] = len;
                q++;
            } else if (len > 0) {
                len = lps[len - 1];   // try the next shorter border, q stays
            } else {
                lps[q] = 0;
                q++;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        StringSearch.validate(text, pattern);
        int n = text.length();
        int m = pattern.length();
        List<Integer> result = new ArrayList<>();
        if (m > n) return result;

        int[] lps = lps(pattern);
        int i = 0;   // index in text: only ever increases
        int j = 0;   // index in pattern
        counters.alignments++;
        while (i < n) {
            counters.charComparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    result.add(i - m);
                    j = lps[j - 1];          // keep going: overlapping matches
                    counters.alignments++;
                }
            } else if (j > 0) {
                j = lps[j - 1];              // shift the pattern, do not move i back
                counters.alignments++;
            } else {
                i++;
                counters.alignments++;
            }
        }
        return result;
    }

    @Override
    public Counters counters() {
        return counters;
    }
}
