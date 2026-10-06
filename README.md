# DAA Assignment 2 — Data Structures

Maven Java project for DAA Assignment 2.

## Implemented

- `DynamicArray` using primitive `int[]` and ×2 growth.
- `MyLinkedList` using custom nodes.
- `MinHeap` using an array-based binary heap.
- `IntList` common interface for the two list structures.
- `steps`, `moves`, and `comparisons` counters following the assignment definitions.
- W1–W4 benchmark with reproducible `Random(42)` input.
- Five warm-up runs + five measured runs; median time is written to CSV.
- W1–W3 setup/fill excluded from measured workload and counters.
- Four workload charts generated directly from `results/results.csv`.
- JUnit 5 tests with edge cases, random reference comparisons, and heap-property checks.
- `REPORT.md` with complexity analysis, loop invariants, benchmark results, and discussion.

## IntelliJ IDEA

1. Open the `DAA_Assignment2` folder in IntelliJ IDEA.
2. Let IntelliJ import `pom.xml` as a Maven project.
3. Use Java 17 or newer.
4. Run `DataStructuresTest` from `src/test/java/daa/DataStructuresTest.java`.
5. Run `daa.Main` to regenerate the CSV and charts.

## Command line

Run tests and build:

```bash
mvn clean test
mvn package
```

Run the benchmark from the **project root** in one line:

```bash
mvn -q -DskipTests package && java -jar target/DAA_Assignment2-1.0.jar
```

The benchmark creates/updates:

```text
results/results.csv
results/plots/w1.png
results/plots/w2.png
results/plots/w3.png
results/plots/w4.png
```

## Benchmark

Sizes: `100`, `1000`, `10000`, `100000`.

- **W1:** 10,000 random `get(index)` calls.
- **W2:** 1,000 `contains` queries, half present and half guaranteed absent.
- **W3:** 1,000 indexed insertions + 1,000 indexed removals at `head` or `middle`.
- **W4:** insert all values into `MinHeap`, then extract all values and verify non-decreasing output.

The benchmark is run from the project root. W1–W3 fill the structure before counters are reset and before timing. Each case has five discarded warm-up runs and five measured runs; a representative `n=100000` warm-up is also performed before the case loop.

## Plot generation

`PlotGenerator.java` reads `results/results.csv` directly and creates one PNG per workload. Each chart uses a time panel and an operation-count panel with logarithmic axes. W3 contains separate head and middle rows in the same file.

## Project structure

```text
src/main/java/daa/
  IntList.java
  DynamicArray.java
  MyLinkedList.java
  MinHeap.java
  Metrics.java
  Benchmark.java
  PlotGenerator.java
  Main.java

src/test/java/daa/
  DataStructuresTest.java

results/
  results.csv
  plots/

REPORT.md
README.md
INTELLIJ_RUN.md
pom.xml
```

## Git

The Git repository, branch history, release tag, and remote URL are recorded in the final project repository. The expected main branch is `main` and the release tag is `v1.0`.
