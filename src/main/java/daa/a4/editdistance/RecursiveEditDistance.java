package daa.a4.editdistance;

import daa.a4.metrics.Counters;

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
        counters.subproblems++;
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
