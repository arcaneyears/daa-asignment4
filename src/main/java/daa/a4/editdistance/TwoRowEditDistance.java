package daa.a4.editdistance;

import daa.a4.metrics.Counters;

/**
 * Version C: the same DP as version B, but only two rows are kept. Time O(n*m), extra memory O(m).
 *
 * State:      cur[j] = edit distance between a[0..i) and b[0..j) for the current row i;
 *             prev[j] holds the same values for row i-1.
 * Transition: cur[j] = prev[j-1] if a[i-1] == b[j-1],
 *             else 1 + min(prev[j], cur[j-1], prev[j-1]); then swap prev and cur.
 * Base case:  prev[j] = j for row 0, and cur[0] = i at the start of row i. Answer is prev[m].
 *
 * Row i only needs row i-1, so older rows can be thrown away.
 */
public final class TwoRowEditDistance implements EditDistance {

    private final Counters counters = new Counters();

    @Override
    public String name() {
        return "ED-C-two-rows";
    }

    @Override
    public int distance(String a, String b) {
        EditDistance.requireNonNull(a, b);
        int n = a.length();
        int m = b.length();

        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        counters.memoryCells += 2L * (m + 1);

        for (int j = 0; j <= m; j++) {
            prev[j] = j;
            counters.subproblems++;
        }

        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            counters.subproblems++;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                counters.charComparisons++;
                if (ca == b.charAt(j - 1)) {
                    cur[j] = prev[j - 1];
                } else {
                    cur[j] = 1 + Math.min(prev[j - 1], Math.min(prev[j], cur[j - 1]));
                }
                counters.subproblems++;
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[m];
    }

    @Override
    public Counters counters() {
        return counters;
    }
}
