package daa.a4.editdistance;

import daa.a4.metrics.Counters;

/**
 * Version A: direct recursion on the last characters, no memory. Slow on purpose.
 *
 * State:      d(i, j) = edit distance between the prefixes a[0..i) and b[0..j).
 * Transition: if a[i-1] == b[j-1] then d(i, j) = d(i-1, j-1);
 *             else d(i, j) = 1 + min(d(i-1, j)   [delete],
 *                                    d(i, j-1)   [insert],
 *                                    d(i-1, j-1) [replace]).
 * Base case:  d(0, j) = j and d(i, 0) = i (only insertions or only deletions are left).
 *
 * The same d(i, j) is recomputed many times, so on strings with no equal characters
 * the number of calls grows exponentially.
 */
public final class RecursiveEditDistance implements EditDistance {

    private final Counters counters = new Counters();
    private String a;
    private String b;

    @Override
    public String name() {
        return "ED-A-recursive";
    }

    @Override
    public int distance(String a, String b) {
        EditDistance.requireNonNull(a, b);
        this.a = a;
        this.b = b;
        return d(a.length(), b.length());
    }

    private int d(int i, int j) {
        counters.subproblems++;                 // one subproblem evaluated = one call
        if (i == 0) return j;
        if (j == 0) return i;
        counters.charComparisons++;
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            return d(i - 1, j - 1);
        }
        int delete = d(i - 1, j);
        int insert = d(i, j - 1);
        int replace = d(i - 1, j - 1);
        return 1 + Math.min(replace, Math.min(delete, insert));
    }

    @Override
    public Counters counters() {
        return counters;
    }
}
