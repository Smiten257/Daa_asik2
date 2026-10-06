package daa;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/** Reproducible benchmark for W1-W4. Setup/fill is never included in W1-W3 measurements. */
public class Benchmark {
    static final int[] NS = {100, 1_000, 10_000, 100_000};
    static final int MEASURED_RUNS = 5;
    static final int WARMUP_RUNS = 5;

    @FunctionalInterface
    interface Workload {
        long run();
    }

    static final class Measurement {
        final long nanos;
        final Metrics metrics;
        Measurement(long nanos, Metrics metrics) {
            this.nanos = nanos;
            this.metrics = metrics;
        }
    }

    static int[] data(int n) {
        Random r = new Random(42);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            // A bounded range lets us construct guaranteed-absent search values below.
            a[i] = r.nextInt(1_000_000_000);
        }
        return a;
    }

    static IntList newList(String structure, Metrics metrics) {
        if (structure.equals("DynamicArray")) {
            // Start from the normal small capacity so the implementation's 2x growth
            // is exercised in ordinary use. Growth belongs to setup, not the workload.
            return new DynamicArray(10, metrics);
        }
        return new MyLinkedList(metrics);
    }

    static void fill(IntList list, int[] data) {
        for (int value : data) list.add(value);
    }

    static void reset(Metrics metrics) {
        metrics.reset();
    }

    static Measurement measure(Workload workload, Metrics metrics) {
        reset(metrics);
        long start = System.nanoTime();
        workload.run();
        long elapsed = System.nanoTime() - start;
        return new Measurement(elapsed, metrics.copy());
    }

    static double medianMillis(long[] nanos) {
        long[] copy = nanos.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2] / 1_000_000.0;
    }

    static Measurement[] measured(Workload workload, Metrics metrics) {
        // Several warm-up runs let the JVM JIT compiler optimize hot methods before timing.
        for (int i = 0; i < WARMUP_RUNS; i++) workload.run();
        Measurement[] results = new Measurement[MEASURED_RUNS];
        for (int i = 0; i < MEASURED_RUNS; i++) {
            results[i] = measure(workload, metrics);
        }
        return results;
    }

    static void row(PrintWriter out, String workload, String variant, String structure,
                    int n, double timeMs, Metrics m) {
        out.printf(Locale.US, "%s,%s,%s,%d,%.3f,%d,%d,%d%n",
                workload, variant, structure, n, timeMs,
                m.steps, m.moves, m.comparisons);
    }

    static void warmUpRepresentativeCases() {
        int n = NS[NS.length - 1];
        int[] a = data(n);

        for (String structure : new String[]{"DynamicArray", "MyLinkedList"}) {
            Metrics m = new Metrics();
            IntList list = newList(structure, m);
            fill(list, a);
            int[] indices = new int[10_000];
            Random r = new Random(42);
            for (int i = 0; i < indices.length; i++) indices[i] = r.nextInt(n);
            for (int pass = 0; pass < 2; pass++) {
                long sink = 0;
                for (int index : indices) sink += list.get(index);
                if (sink == Long.MIN_VALUE) throw new AssertionError();
            }

            int[] queries = new int[1_000];
            r = new Random(42);
            for (int i = 0; i < queries.length; i++) queries[i] = (i % 2 == 0) ? a[r.nextInt(n)] : -1;
            for (int pass = 0; pass < 2; pass++) {
                int found = 0;
                for (int q : queries) if (list.contains(q)) found++;
                if (found < 0) throw new AssertionError();
            }

            for (String variant : new String[]{"head", "middle"}) {
                int index = variant.equals("head") ? 0 : n / 2;
                for (int pass = 0; pass < 2; pass++) {
                    for (int i = 0; i < 1_000; i++) list.add(index, i);
                    for (int i = 0; i < 1_000; i++) list.remove(index);
                }
            }
        }

        Metrics heapMetrics = new Metrics();
        MinHeap heap = new MinHeap(10, heapMetrics);
        for (int value : a) heap.insert(value);
        for (int i = 0; i < n; i++) heap.extractMin();
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        new File("results/plots").mkdirs();
        warmUpRepresentativeCases();

        try (PrintWriter out = new PrintWriter("results/results.csv")) {
            out.println("workload,variant,structure,n,time_ms,steps,moves,comparisons");

            for (int n : NS) {
                int[] a = data(n);

                // W1: fill first, reset counters, then time only the 10,000 random gets.
                for (String structure : new String[]{"DynamicArray", "MyLinkedList"}) {
                    Metrics m = new Metrics();
                    IntList list = newList(structure, m);
                    fill(list, a);
                    Random indices = new Random(42);
                    int[] queryIndices = new int[10_000];
                    for (int i = 0; i < queryIndices.length; i++) queryIndices[i] = indices.nextInt(n);

                    Workload op = () -> {
                        long sink = 0;
                        for (int index : queryIndices) sink += list.get(index);
                        return sink;
                    };
                    Measurement[] ms = measured(op, m);
                    long[] times = Arrays.stream(ms).mapToLong(x -> x.nanos).toArray();
                    Measurement chosen = ms[medianIndex(times)];
                    row(out, "W1", "-", structure, n, medianMillis(times), chosen.metrics);
                }

                // W2: random present indices from the whole structure and guaranteed-absent values.
                for (String structure : new String[]{"DynamicArray", "MyLinkedList"}) {
                    Metrics m = new Metrics();
                    IntList list = newList(structure, m);
                    fill(list, a);
                    Random r = new Random(42);
                    int[] queries = new int[1_000];
                    for (int i = 0; i < queries.length; i++) {
                        if (i % 2 == 0) {
                            queries[i] = a[r.nextInt(n)];
                        } else {
                            // Data values are in [0, 1_000_000_000), so -1 is guaranteed absent.
                            queries[i] = -1;
                        }
                    }
                    Workload op = () -> {
                        long found = 0;
                        for (int q : queries) if (list.contains(q)) found++;
                        return found;
                    };
                    Measurement[] ms = measured(op, m);
                    long[] times = Arrays.stream(ms).mapToLong(x -> x.nanos).toArray();
                    Measurement chosen = ms[medianIndex(times)];
                    row(out, "W2", "-", structure, n, medianMillis(times), chosen.metrics);
                }

                // W3: fill first; only the 1,000 insertions + 1,000 removals are measured/counted.
                for (String variant : new String[]{"head", "middle"}) {
                    for (String structure : new String[]{"DynamicArray", "MyLinkedList"}) {
                        Metrics m = new Metrics();
                        IntList list = newList(structure, m);
                        fill(list, a);
                        int index = variant.equals("head") ? 0 : n / 2;
                        Workload op = () -> {
                            for (int i = 0; i < 1_000; i++) list.add(index, i);
                            for (int i = 0; i < 1_000; i++) list.remove(index);
                            return list.size();
                        };
                        Measurement[] ms = measured(op, m);
                        long[] times = Arrays.stream(ms).mapToLong(x -> x.nanos).toArray();
                        Measurement chosen = ms[medianIndex(times)];
                        row(out, "W3", variant, structure, n, medianMillis(times), chosen.metrics);
                    }
                }

                // W4: priority processing includes both insertion and extraction as the workload specifies.
                long[] times = new long[MEASURED_RUNS];
                Metrics medianMetrics = new Metrics();
                // Several warm-up runs on separate heaps.
                for (int warm = 0; warm < WARMUP_RUNS; warm++) {
                    Metrics warmMetrics = new Metrics();
                    MinHeap warmHeap = new MinHeap(10, warmMetrics);
                    for (int value : a) warmHeap.insert(value);
                    long last = Long.MIN_VALUE;
                    for (int i = 0; i < a.length; i++) {
                        int value = warmHeap.extractMin();
                        if (value < last) throw new AssertionError("heap output not sorted");
                        last = value;
                    }
                }
                for (int run = 0; run < MEASURED_RUNS; run++) {
                    Metrics runMetrics = new Metrics();
                    MinHeap runHeap = new MinHeap(10, runMetrics);
                    long start = System.nanoTime();
                    for (int value : a) runHeap.insert(value);
                    long last = Long.MIN_VALUE;
                    for (int i = 0; i < a.length; i++) {
                        int value = runHeap.extractMin();
                        if (value < last) throw new AssertionError("heap output not sorted");
                        last = value;
                    }
                    times[run] = System.nanoTime() - start;
                }
                // Operation counts are deterministic for the same input; use the exact median-run counts.
                medianMetrics.reset();
                MinHeap finalHeap = new MinHeap(10, medianMetrics);
                long last = Long.MIN_VALUE;
                for (int value : a) finalHeap.insert(value);
                for (int i = 0; i < a.length; i++) {
                    int value = finalHeap.extractMin();
                    if (value < last) throw new AssertionError("heap output not sorted");
                    last = value;
                }
                row(out, "W4", "-", "MinHeap", n, medianMillis(times), medianMetrics);
            }
        }

        PlotGenerator.generateAll();
    }

    static int medianIndex(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        long median = sorted[sorted.length / 2];
        for (int i = 0; i < values.length; i++) if (values[i] == median) return i;
        return values.length / 2;
    }
}
