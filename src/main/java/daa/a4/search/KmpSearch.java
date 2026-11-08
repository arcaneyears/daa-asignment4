package daa.a4.search;

import daa.a4.metrics.Counters;

import java.util.ArrayList;
import java.util.List;

public final class KmpSearch implements StringSearch {
    private final Counters counters = new Counters();

    @Override
    public String name() {
        return "KMP";
    }

    public int[] lps(String p) {
        if (p == null) throw new IllegalArgumentException("Pattern must not be null");
        int m = p.length();
        int[] lps = new int[m];
        counters.memoryCells += m;
        int len = 0;
        int q = 1;
        while (q < m) {
            counters.preprocessComparisons++;
            if (p.charAt(q) == p.charAt(len)) {
                len++;
                lps[q] = len;
                q++;
            } else if (len > 0) {
                len = lps[len - 1];
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
        int i = 0;
        int j = 0;
        counters.alignments++;
        while (i < n) {
            counters.charComparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    result.add(i - m);
                    j = lps[j - 1];
                    counters.alignments++;
                }
            } else if (j > 0) {
                j = lps[j - 1];
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
