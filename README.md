# DAA Assignment 4 — Edit Distance & String Search

Yersaiyn Taubay, SE-2524

City Search & Spell-Check: Edit Distance in three versions (recursive, full DP table, two rows) and string search (Naive and KMP), with work counters, a seeded input generator, an experiment that writes `results/results.csv`, and PNG plots. The analysis is in [REPORT.md](REPORT.md).

## Requirements

- **JDK 17 or newer** (tested with JDK 21)
- **Maven 3.8+** — or IntelliJ IDEA, which has Maven built in
- Nothing else: the only dependency is JUnit 5 (downloaded by Maven), and the plots are drawn with plain Java (`java.awt`), so no Python or chart library is needed.

## Build and run tests

```bash
mvn test
```

## Run the experiment

```bash
mvn compile exec:java
```

This takes about 15–30 seconds and writes:

- `results/results.csv` — one row per measured run (5 runs per configuration after 1 warm-up run)
- `results/summary.csv` — median time and counters per configuration
- `results/plots/*.png` — the 6 plots used in the report

Options (pass with `-Dexec.args="..."`):

| Option | Meaning |
|---|---|
| `--quick` | small sizes, finishes in under a second (to check that everything works) |
| `--seed N` | base seed of the generator (default `20261112`) |
| `--runs N` | measured runs per configuration, 3–5 (default 5) |
| `--out DIR` | output folder (default `results`) |

Example: `mvn compile exec:java -Dexec.args="--quick --out quick-results"`

Redraw the plots from an existing CSV only:

```bash
mvn compile exec:java -Dexec.mainClass=daa.a4.experiment.Plots
```

The same command always gives the same inputs and the same counters; only `time_ms` changes from run to run.

## Running in IntelliJ IDEA (Windows)

1. **File → Open** and choose the project folder (the one with `pom.xml`). IntelliJ imports it as a Maven project and downloads JUnit.
2. If asked, set the Project SDK to JDK 17 or newer (**File → Project Structure → Project**).
3. Tests: right-click `src/test/java` → **Run 'All Tests'**.
4. Experiment: open `src/main/java/daa/a4/experiment/ExperimentRunner.java` and click the green ▶ next to `main`. The working directory must be the project folder (IntelliJ's default), so the results land in `results/`.

## Project layout

```
src/main/java/daa/a4/
  editdistance/  EditDistance (interface), RecursiveEditDistance (A), TableEditDistance (B), TwoRowEditDistance (C)
  search/        StringSearch (interface), NaiveSearch, KmpSearch (public int[] lps(String))
  metrics/       Counters — counted inside the algorithms
  experiment/    InputGenerator (seeded), ExperimentRunner, Plots, SimpleChart (PNG drawing)
src/test/java/daa/a4/
  editdistance/EditDistanceTest.java   reference values, empty strings, 100 random cross-checks
  search/StringSearchTest.java         LPS, reference matches, overlaps, edge cases, Naive vs KMP on 100 random texts
results/results.csv, results/summary.csv, results/plots/
REPORT.md
```

## Counters

| Counter | Meaning |
|---|---|
| `subproblems` | Edit Distance: d(i,j) evaluations (recursive calls for A, table cells written for B and C) |
| `char_comparisons` | character comparisons in the main phase |
| `preprocess_comparisons` | KMP: comparisons while building the LPS array |
| `memory_cells` | int cells allocated (table, two rows, LPS array) |
| `alignments` | string search: pattern positions tried |

## Git

Work is on `main`; the final version is tagged `v1.0`.

GitHub repository: `<add your repository link here>`

## AI use acknowledgement

This project was produced with the help of Claude (Anthropic, model Claude Opus 5.5). Claude generated the Java code, the JUnit tests, the input generator, the experiment runner and the plotting code, proposed the Experiment Design Card, ran the experiment and drafted REPORT.md. I reviewed the code and the report, ran the tests and the experiment myself, and I am responsible for the final submission.
