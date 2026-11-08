package daa.a4.editdistance;

import daa.a4.metrics.Counters;

/**
 * Version B: bottom-up DP with an (n+1) x (m+1) table. Time O(n*m), memory O(n*m).
 *
 * State:      dp[i][j] = edit distance between the prefixes a[0..i) and b[0..j).
 * Transition: dp[i][j] = dp[i-1][j-1] if a[i-1] == b[j-1],
 *             else 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]).
 * Base case:  dp[0][j] = j, dp[i][0] = i. Answer is dp[n][m].
 */
public final class TableEditDistance implements EditDistance {

    private final Counters counters = new Counters();

    @Override
    public String name() {
        return "ED-B-table";
    }

    @Override
    public int distance(String a, String b) {
        EditDistance.requireNonNull(a, b);
        int n = a.length();
        int m = b.length();

        int[][] dp = new int[n + 1][m + 1];
        counters.memoryCells += (long) (n + 1) * (m + 1);

        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
            counters.subproblems++;
        }
        for (int j = 1; j <= m; j++) {
            dp[0][j] = j;
            counters.subproblems++;
        }

        for (int i = 1; i <= n; i++) {
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                counters.charComparisons++;
                if (ca == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
                counters.subproblems++;
            }
        }
        return dp[n][m];
    }

    @Override
    public Counters counters() {
        return counters;
    }
}
