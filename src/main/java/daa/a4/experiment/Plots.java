package daa.a4.experiment;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Reads results.csv, takes the median time over the runs of every configuration,
 * writes results/summary.csv and draws the PNG plots into results/plots/.
 *
 * Can also be run alone: mvn -q compile exec:java -Dexec.mainClass=daa.a4.experiment.Plots
 */
public final class Plots {

    /** One configuration (algorithm, input, n, m) with the median time over its runs. */
    record Point(String algorithm, String family, String input, int n, int m, int runs,
                 double medianMs, long subproblems, long comparisons, long memoryCells, long result) {
    }

    public static void main(String[] args) throws IOException {
        Path csv = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        drawAll(csv, csv.getParent().resolve("plots"));
    }

    public static void drawAll(Path csv, Path plotsDir) throws IOException {
        System.setProperty("java.awt.headless", "true");
        Files.createDirectories(plotsDir);
        List<Point> pts = aggregate(csv);
        writeSummary(pts, csv.resolveSibling("summary.csv"));

        // 1. Recursive Edit Distance: calls vs n, log y
        SimpleChart c1 = chart("Edit Distance A (recursive): subproblem calls vs n",
                "string length n = m (characters)", "recursive calls (count, log scale)", false, true);
        for (String in : ExperimentRunner.ED_INPUTS) {
            add(c1, "A-recursive, " + in, pts, p -> p.algorithm().equals("ED-A-recursive") && p.input().equals(in),
                    p -> p.subproblems());
        }
        save(c1, plotsDir, "1_ed_recursive_calls");

        // 2. Table vs two rows: time vs n, log-log
        SimpleChart c2 = chart("Edit Distance B (table) vs C (two rows): median time vs n",
                "string length n = m (characters, log scale)", "median time of 5 runs (ms, log scale)", true, true);
        for (String alg : List.of("ED-B-table", "ED-C-two-rows")) {
            for (String in : ExperimentRunner.ED_INPUTS) {
                add(c2, alg.substring(3) + ", " + in, pts,
                        p -> p.algorithm().equals(alg) && p.input().equals(in), Point::medianMs);
            }
        }
        save(c2, plotsDir, "2_ed_dp_time");

        // 3. Memory: table vs two rows
        SimpleChart c3 = chart("Edit Distance: extra memory allocated vs n",
                "string length n = m (characters, log scale)", "int cells allocated (count, log scale)", true, true);
        for (String alg : List.of("ED-B-table", "ED-C-two-rows")) {
            add(c3, alg.substring(3), pts, p -> p.algorithm().equals(alg) && p.input().equals("typo"),
                    p -> p.memoryCells());
        }
        save(c3, plotsDir, "3_ed_memory");

        // 4. Search: comparisons per text character (ratio)
        SimpleChart c4 = chart("String search: character comparisons per text character",
                "text length n (characters, log scale)", "total comparisons / n (ratio)", true, true);
        for (String alg : List.of("Naive", "KMP")) {
            for (String in : ExperimentRunner.SEARCH_INPUTS) {
                add(c4, alg + ", " + in, pts, p -> p.algorithm().equals(alg) && p.input().equals(in),
                        p -> (double) p.comparisons() / p.n());
            }
        }
        save(c4, plotsDir, "4_search_comparisons_ratio");

        // 5. Worst case: comparisons vs pattern length m
        SimpleChart c5 = chart("String search, worst input (text A...A, pattern A...AB): comparisons vs m",
                "pattern length m (characters, log scale)", "total comparisons (count, log scale)", true, true);
        for (String alg : List.of("Naive", "KMP")) {
            addXm(c5, alg, pts, p -> p.algorithm().equals(alg) && p.input().equals("worst-vary-m"),
                    p -> p.comparisons());
        }
        save(c5, plotsDir, "5_search_vary_m");

        // 6. Search time vs n
        SimpleChart c6 = chart("String search: median time vs n",
                "text length n (characters, log scale)", "median time of 5 runs (ms, log scale)", true, true);
        for (String alg : List.of("Naive", "KMP")) {
            for (String in : ExperimentRunner.SEARCH_INPUTS) {
                add(c6, alg + ", " + in, pts, p -> p.algorithm().equals(alg) && p.input().equals(in),
                        Point::medianMs);
            }
        }
        save(c6, plotsDir, "6_search_time");
    }

    // ------------------------------------------------------------------ data

    static List<Point> aggregate(Path csv) throws IOException {
        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        String[] header = lines.get(0).split(",");
        Map<String, Integer> col = new LinkedHashMap<>();
        for (int i = 0; i < header.length; i++) col.put(header[i], i);

        Map<String, List<String[]>> groups = new LinkedHashMap<>();
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) continue;
            String[] f = line.split(",");
            String key = f[col.get("algorithm")] + "|" + f[col.get("input")] + "|" + f[col.get("n")] + "|" + f[col.get("m")];
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(f);
        }
        List<Point> pts = new ArrayList<>();
        for (List<String[]> g : groups.values()) {
            double[] times = g.stream().mapToDouble(f -> Double.parseDouble(f[col.get("time_ms")])).sorted().toArray();
            String[] f = g.get(0);
            pts.add(new Point(f[col.get("algorithm")], f[col.get("family")], f[col.get("input")],
                    Integer.parseInt(f[col.get("n")]), Integer.parseInt(f[col.get("m")]), g.size(),
                    median(times),
                    Long.parseLong(f[col.get("subproblems")]),
                    Long.parseLong(f[col.get("total_comparisons")]),
                    Long.parseLong(f[col.get("memory_cells")]),
                    Long.parseLong(f[col.get("result")])));
        }
        return pts;
    }

    static double median(double[] sorted) {
        int k = sorted.length;
        return k % 2 == 1 ? sorted[k / 2] : (sorted[k / 2 - 1] + sorted[k / 2]) / 2.0;
    }

    private static void writeSummary(List<Point> pts, Path out) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
            pw.println("algorithm,input,n,m,runs,median_time_ms,subproblems,total_comparisons,"
                    + "comparisons_per_n,memory_cells,result");
            for (Point p : pts) {
                pw.printf(Locale.ROOT, "%s,%s,%d,%d,%d,%.4f,%d,%d,%.4f,%d,%d%n", p.algorithm(), p.input(), p.n(), p.m(),
                        p.runs(), p.medianMs(), p.subproblems(), p.comparisons(), (double) p.comparisons() / p.n(),
                        p.memoryCells(), p.result());
            }
        }
    }

    // ------------------------------------------------------------------ drawing

    private static SimpleChart chart(String title, String xTitle, String yTitle, boolean logX, boolean logY) {
        return new SimpleChart(title, xTitle, yTitle, logX, logY);
    }

    private static void add(SimpleChart c, String name, List<Point> pts, Predicate<Point> filter, ToDoubleFunction<Point> y) {
        addSeries(c, name, pts, filter, p -> p.n(), y);
    }

    private static void addXm(SimpleChart c, String name, List<Point> pts, Predicate<Point> filter, ToDoubleFunction<Point> y) {
        addSeries(c, name, pts, filter, p -> p.m(), y);
    }

    private static void addSeries(SimpleChart c, String name, List<Point> pts, Predicate<Point> filter,
                                  ToDoubleFunction<Point> x, ToDoubleFunction<Point> y) {
        List<Point> sel = pts.stream().filter(filter).sorted(Comparator.comparingDouble(x)).toList();
        if (sel.isEmpty()) return;
        double[] xs = sel.stream().mapToDouble(x).toArray();
        double[] ys = sel.stream().mapToDouble(y).map(v -> Math.max(v, 1e-4)).toArray();
        c.addSeries(name, xs, ys, name.startsWith("KMP") || name.contains("two-rows"));
    }

    private static void save(SimpleChart c, Path dir, String name) throws IOException {
        if (c.isEmpty()) return;
        Path file = dir.resolve(name + ".png");
        c.save(file);
        System.out.println("Plot: " + file);
    }
}
