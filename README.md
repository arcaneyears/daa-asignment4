# DAA Assignment 4 — Edit Distance & String Search

Yersaiyn Taubay · SE-2524

City Search & Spell-Check, written from scratch in Java (only `java.util` and `java.awt` are used):
Edit Distance in three versions (recursive, full DP table, two rows) and string search (Naive and KMP).
Every algorithm counts its own work (`subproblems`, `char_comparisons`, `preprocess_comparisons`,
`memory_cells`, `alignments`), and a seeded experiment writes the CSV results and the plots.
The analysis is in [REPORT.md](REPORT.md) ([PDF](REPORT.pdf)).

## Requirements

- JDK 17 or newer (tested with OpenJDK 21)
- Maven 3.8+
- Nothing else: the only dependency is JUnit 5, and the plots are drawn with plain `java.awt`

## Build and test

```bash
mvn clean test          # compiles and runs all JUnit 5 tests
mvn clean package       # same + builds target/assignment4-1.0.jar
```

## Run the experiment (one command)

```bash
mvn -q compile exec:java
```

Options (pass with `-Dexec.args="..."`):

| Option | Meaning |
|---|---|
| `--quick` | small sizes, finishes in under a second |
| `--seed N` | base seed of the generator (default `20261112`) |
| `--runs N` | measured runs per configuration, 3–5 (default 5) |
| `--out DIR` | output folder (default `results`) |

```bash
mvn -q compile exec:java -Dexec.args="--quick --out quick-results"
mvn -q compile exec:java -Dexec.mainClass=daa.a4.experiment.Plots    # redraw plots from results.csv
```

The experiment builds every input from a seeded `Random`, runs each configuration once as a warm-up
and then 5 times (median time). The same command always gives the same inputs and the same counters;
only `time_ms` changes between runs. A full run takes about 15–30 s.

Outputs:

| File | Content |
|---|---|
| `results/results.csv` | one row per measured run, columns `algorithm,family,input,n,m,run,time_ms,subproblems,char_comparisons,preprocess_comparisons,total_comparisons,memory_cells,alignments,result` |
| `results/summary.csv` | median time and counters per configuration |
| `results/plots/*.png` | the 6 plots used in the report |

## Project layout

```
src/main/java/daa/a4/
  editdistance/  EditDistance (interface), RecursiveEditDistance (A), TableEditDistance (B), TwoRowEditDistance (C)
  search/        StringSearch (interface), NaiveSearch, KmpSearch (public int[] lps(String))
  metrics/       Counters (subproblems, comparisons, memory cells, alignments)
  experiment/    InputGenerator (seeded), ExperimentRunner, Plots, SimpleChart (PNG drawing)
src/test/java/daa/a4/   JUnit 5 tests
results/                results.csv, summary.csv, plots/
REPORT.md, REPORT.pdf   the report
```

### Inputs

| Input | Family | Used for |
|---|---|---|
| `identical` | Edit Distance | a = b, best case for the recursion |
| `typo` | Edit Distance | b = a with ~10 % replacements ("Abylai Han" vs "Abylai Khan") |
| `disjoint` | Edit Distance | no common character, worst case for the recursion |
| `random` | search | random lowercase text, pattern of length 10 |
| `logs` | search | generated log lines, pattern `ERROR 503 /api/streets` |
| `worst` | search | text `A…A`, pattern `A…AB`, worst case for Naive |

## Tests

| Test class | What it checks |
|---|---|
| `EditDistanceTest` | reference values, empty strings, typo scenario, symmetry and bounds, all three versions agree on 100 random strings, table vs two rows counters |
| `StringSearchTest` | LPS reference arrays, reference and overlapping matches, empty pattern, pattern longer than text, Naive == KMP on 100 random texts, KMP ≤ 2n comparisons |

GitHub Actions runs the tests and a quick experiment on every push to `main`.

## Git

Work is on `main`; the release is tagged `v1.0`.
