package daa.a4.metrics;

public final class Counters {
    public long subproblems;

    public long charComparisons;

    public long preprocessComparisons;

    public long memoryCells;

    public long alignments;

    public void reset() {
        subproblems = 0;
        charComparisons = 0;
        preprocessComparisons = 0;
        memoryCells = 0;
        alignments = 0;
    }

    public long totalComparisons() {
        return charComparisons + preprocessComparisons;
    }
}
