package daa.a4.metrics;

/**
 * Work counters that an algorithm increases while it runs.
 * Every value here is counted inside the algorithm, never computed from a formula afterwards.
 */
public final class Counters {

    /** Edit Distance: number of subproblems d(i,j) evaluated (recursive calls or table cells written). */
    public long subproblems;

    /** Number of character-to-character comparisons (a[i]==b[j], or text[i]==pattern[j]). */
    public long charComparisons;

    /** KMP only: character comparisons spent while building the LPS array. */
    public long preprocessComparisons;

    /** Number of int cells of extra memory the algorithm allocated (DP table, rows, LPS array). */
    public long memoryCells;

    /** String search: number of alignments of the pattern against the text that were tried. */
    public long alignments;

    public void reset() {
        subproblems = 0;
        charComparisons = 0;
        preprocessComparisons = 0;
        memoryCells = 0;
        alignments = 0;
    }

    /** Total character comparisons, including the preprocessing phase. */
    public long totalComparisons() {
        return charComparisons + preprocessComparisons;
    }
}
