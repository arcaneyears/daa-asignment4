package daa.a4.search;

import daa.a4.metrics.Counters;

import java.util.ArrayList;
import java.util.List;

public final class NaiveSearch implements StringSearch {
    private final Counters counters = new Counters();

    @Override
    public String name() {
        return "Naive";
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        StringSearch.validate(text, pattern);
        int n = text.length();
        int m = pattern.length();
        List<Integer> result = new ArrayList<>();
        if (m > n) return result;

        for (int s = 0; s <= n - m; s++) {
            counters.alignments++;
            int j = 0;
            while (j < m) {
                counters.charComparisons++;
                if (text.charAt(s + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == m) result.add(s);
        }
        return result;
    }

    @Override
    public Counters counters() {
        return counters;
    }
}
