package daa.a4.experiment;

import daa.a4.editdistance.EditDistance;
import daa.a4.editdistance.RecursiveEditDistance;
import daa.a4.editdistance.TableEditDistance;
import daa.a4.editdistance.TwoRowEditDistance;
import daa.a4.metrics.Counters;
import daa.a4.search.KmpSearch;
import daa.a4.search.NaiveSearch;
import daa.a4.search.StringSearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ExperimentRunner {
    static final int[] ED_RECURSIVE_SIZES = {3, 6, 8, 10, 11, 12};
    static final int[] ED_DP_SIZES = {250, 500, 1000, 2000, 4000};
    static final String[] ED_INPUTS = {"identical", "typo", "disjoint"};

    static final int[] SEARCH_SIZES = {125_000, 250_000, 500_000, 1_000_000, 2_000_000};
    static final String[] SEARCH_INPUTS = {"random", "logs", "worst"};
    static final int WORST_PATTERN_LENGTH = 100;

    static final int VARY_M_TEXT = 200_000;
    static final int[] VARY_M_SIZES = {10, 40, 160, 640};

    private static final String HEADER =
            "algorithm,family,input,n,m,run,time_ms,subproblems,char_comparisons,"
                    + "preprocess_comparisons,total_comparisons,memory_cells,alignments,result";

    private final InputGenerator gen;
    private final int runs;
    private final PrintWriter out;

    private ExperimentRunner(InputGenerator gen, int runs, PrintWriter out) {
        this.gen = gen;
        this.runs = runs;
        this.out = out;
    }

    public static void main(String[] args) throws IOException {
        long seed = InputGenerator.DEFAULT_SEED;
        int runs = 5;
        boolean quick = false;
        Path outDir = Path.of("results");
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--seed" -> seed = Long.parseLong(args[++i]);
                case "--runs" -> runs = Integer.parseInt(args[++i]);
                case "--quick" -> quick = true;
                case "--out" -> outDir = Path.of(args[++i]);
                default -> throw new IllegalArgumentException("Unknown option " + args[i]);
            }
        }
        if (runs < 3 || runs > 5) throw new IllegalArgumentException("--runs must be 3..5");

        int[] recSizes = quick ? new int[]{2, 4, 6, 8} : ED_RECURSIVE_SIZES;
        int[] dpSizes = quick ? new int[]{50, 100, 200, 400} : ED_DP_SIZES;
        int[] searchSizes = quick ? new int[]{10_000, 20_000, 40_000, 80_000} : SEARCH_SIZES;
        int[] varyM = quick ? new int[]{5, 10, 20, 40} : VARY_M_SIZES;
        int varyMText = quick ? 20_000 : VARY_M_TEXT;

        Files.createDirectories(outDir.resolve("plots"));
        Path csv = outDir.resolve("results.csv");
        long start = System.nanoTime();
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(csv, StandardCharsets.UTF_8))) {
            pw.println(HEADER);
            ExperimentRunner r = new ExperimentRunner(new InputGenerator(seed), runs, pw);
            r.runEditDistance(recSizes, dpSizes);
            r.runSearchVaryN(searchSizes);
            r.runSearchVaryM(varyMText, varyM);
        }
        System.out.printf(Locale.ROOT, "Wrote %s in %.1f s%n", csv, (System.nanoTime() - start) / 1e9);
        Plots.drawAll(csv, outDir.resolve("plots"));
    }

    private void runEditDistance(int[] recSizes, int[] dpSizes) {
        EditDistance recursive = new RecursiveEditDistance();
        List<EditDistance> dp = List.of(new TableEditDistance(), new TwoRowEditDistance());

        for (String input : ED_INPUTS) {
            for (int n : recSizes) {
                String[] ab = gen.edInput(input, n);
                int expected = new TwoRowEditDistance().distance(ab[0], ab[1]);
                measureEditDistance(recursive, input, ab, expected);
            }
            for (int n : dpSizes) {
                String[] ab = gen.edInput(input, n);
                Integer expected = null;
                for (EditDistance ed : dp) {
                    int d = measureEditDistance(ed, input, ab, expected);
                    expected = d;
                }
            }
        }
    }

    private int measureEditDistance(EditDistance ed, String input, String[] ab, Integer expected) {
        ed.distance(ab[0], ab[1]);
        int result = -1;
        for (int run = 1; run <= runs; run++) {
            ed.counters().reset();
            long t0 = System.nanoTime();
            result = ed.distance(ab[0], ab[1]);
            long t1 = System.nanoTime();
            if (expected != null && result != expected) {
                throw new IllegalStateException(ed.name() + " disagrees on " + input + " n=" + ab[0].length());
            }
            write(ed.name(), "edit-distance", input, ab[0].length(), ab[1].length(), run, t1 - t0,
                    ed.counters(), result);
        }
        System.out.printf(Locale.ROOT, "  %-14s %-9s n=%-5d d=%d%n", ed.name(), input, ab[0].length(), result);
        return result;
    }

    private void runSearchVaryN(int[] sizes) {
        for (String input : SEARCH_INPUTS) {
            for (int n : sizes) {
                String[] tp = switch (input) {
                    case "random" -> gen.searchRandom(n);
                    case "logs" -> gen.searchLogs(n);
                    case "worst" -> gen.searchWorst(n, WORST_PATTERN_LENGTH);
                    default -> throw new IllegalStateException();
                };
                measureBoth(input, tp);
            }
        }
    }

    private void runSearchVaryM(int textLength, int[] patternLengths) {
        for (int m : patternLengths) {
            measureBoth("worst-vary-m", gen.searchWorst(textLength, m));
        }
    }

    private void measureBoth(String input, String[] tp) {
        List<Integer> naive = measureSearch(new NaiveSearch(), input, tp);
        List<Integer> kmp = measureSearch(new KmpSearch(), input, tp);
        if (!Objects.equals(naive, kmp)) {
            throw new IllegalStateException("Naive and KMP disagree on " + input + " n=" + tp[0].length());
        }
    }

    private List<Integer> measureSearch(StringSearch s, String input, String[] tp) {
        s.search(tp[0], tp[1]);
        List<Integer> result = new ArrayList<>();
        for (int run = 1; run <= runs; run++) {
            s.counters().reset();
            long t0 = System.nanoTime();
            result = s.search(tp[0], tp[1]);
            long t1 = System.nanoTime();
            write(s.name(), "string-search", input, tp[0].length(), tp[1].length(), run, t1 - t0,
                    s.counters(), result.size());
        }
        System.out.printf(Locale.ROOT, "  %-6s %-13s n=%-8d m=%-4d matches=%d%n",
                s.name(), input, tp[0].length(), tp[1].length(), result.size());
        return result;
    }

    private void write(String algorithm, String family, String input, int n, int m, int run, long nanos,
                       Counters c, long result) {
        out.printf(Locale.ROOT, "%s,%s,%s,%d,%d,%d,%.4f,%d,%d,%d,%d,%d,%d,%d%n",
                algorithm, family, input, n, m, run, nanos / 1e6,
                c.subproblems, c.charComparisons, c.preprocessComparisons, c.totalComparisons(),
                c.memoryCells, c.alignments, result);
    }
}
