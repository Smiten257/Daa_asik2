package daa;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads results/results.csv and creates one two-panel PNG per workload. */
public final class PlotGenerator {
    private PlotGenerator() {}

    private static final int W = 1500;
    private static final int H = 760;
    private static final int LEFT = 95;
    private static final int RIGHT = 220;
    private static final int TOP = 92;
    private static final int BOTTOM = 82;
    private static final int GAP = 90;
    private static final int[] X_TICKS = {100, 1_000, 10_000, 100_000};

    private static final Color ARRAY = new Color(35, 100, 210);
    private static final Color LIST = new Color(210, 55, 55);
    private static final Color HEAP = new Color(40, 155, 75);
    private static final Color GRID = new Color(225, 225, 225);
    private static final Color AXIS = new Color(35, 35, 35);

    enum Metric { STEPS, MOVES, COMPARISONS }

    static final class Row {
        String workload, variant, structure;
        int n;
        double time;
        long steps, moves, comparisons;
    }

    static final class Scale {
        final double min, max;
        Scale(double min, double max) { this.min = min; this.max = max; }
    }

    static final class Series {
        String name;
        String structure;
        String variant;
        Metric metric;
        List<Row> rows;
        Color color;
        boolean dashed;
        Series(String name, String structure, String variant, Metric metric,
               List<Row> rows, Color color, boolean dashed) {
            this.name = name; this.structure = structure; this.variant = variant;
            this.metric = metric; this.rows = rows; this.color = color; this.dashed = dashed;
        }
    }

    public static void generateAll() throws Exception {
        List<Row> rows = read();
        for (String workload : new String[]{"W1", "W2", "W3", "W4"}) {
            generateWorkload(rows, workload);
        }
    }

    static List<Row> read() throws Exception {
        List<Row> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("results/results.csv"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",");
                Row r = new Row();
                r.workload = p[0];
                r.variant = p[1];
                r.structure = p[2];
                r.n = Integer.parseInt(p[3]);
                r.time = Double.parseDouble(p[4]);
                r.steps = Long.parseLong(p[5]);
                r.moves = Long.parseLong(p[6]);
                r.comparisons = Long.parseLong(p[7]);
                rows.add(r);
            }
        }
        return rows;
    }

    static void generateWorkload(List<Row> rows, String workload) throws Exception {
        List<Row> selected = rows.stream().filter(r -> r.workload.equals(workload)).toList();
        if (workload.equals("W3")) {
            plotW3(selected);
        } else {
            BufferedImageBuilder image = new BufferedImageBuilder(W, H);
            Graphics2D g = image.g;
            panelFrame(g, 40, 25, 700, H - 60, workload + " — time", "time, ms (median of 5 runs)");
            panelFrame(g, 790, 25, 670, H - 60, workload + " — operation counts", "operation count");
            drawTimePanel(g, selected, 40, 25, 700, H - 60);
            drawOperationsPanel(g, selected, 790, 25, 670, H - 60);
            ImageIO.write(image.image, "png", new File("results/plots/" + workload.toLowerCase() + ".png"));
            image.dispose();
        }
    }

    static void plotW3(List<Row> rows) throws Exception {
        int w = 1500, h = 1080;
        BufferedImageBuilder image = new BufferedImageBuilder(w, h);
        Graphics2D g = image.g;
        panelFrame(g, 40, 25, 700, 500, "W3 head — time", "time, ms (median of 5 runs)");
        panelFrame(g, 790, 25, 670, 500, "W3 head — operation counts", "operation count");
        panelFrame(g, 40, 550, 700, 500, "W3 middle — time", "time, ms (median of 5 runs)");
        panelFrame(g, 790, 550, 670, 500, "W3 middle — operation counts", "operation count");
        List<Row> head = rows.stream().filter(r -> r.variant.equals("head")).toList();
        List<Row> middle = rows.stream().filter(r -> r.variant.equals("middle")).toList();
        drawTimePanel(g, head, 40, 25, 700, 500);
        drawOperationsPanel(g, head, 790, 25, 670, 500);
        drawTimePanel(g, middle, 40, 550, 700, 500);
        drawOperationsPanel(g, middle, 790, 550, 670, 500);
        ImageIO.write(image.image, "png", new File("results/plots/w3.png"));
        image.dispose();
    }

    static void panelFrame(Graphics2D g, int x0, int y0, int width, int height, String title, String yLabel) {
        g.setColor(Color.WHITE);
        g.fillRect(x0, y0, width, height);
        g.setColor(AXIS);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString(title, x0 + 20, y0 + 34);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("n (elements in the structure)", x0 + width / 2 - 82, y0 + height - 15);
        g.rotate(-Math.PI / 2);
        g.drawString(yLabel, -(y0 + height / 2 + 45), x0 + 19);
        g.rotate(Math.PI / 2);
        g.setColor(new Color(205, 205, 205));
        g.drawRect(x0, y0, width, height);
    }

    static void drawTimePanel(Graphics2D g, List<Row> rows, int x0, int y0, int width, int height) {
        int left = x0 + LEFT;
        int right = x0 + width - 175;
        int top = y0 + TOP;
        int bottom = y0 + height - BOTTOM;
        Scale scale = timeScale(rows);
        axes(g, left, right, top, bottom, scale, "time, ms (median of 5 runs)");

        Map<String, List<Row>> grouped = groupByStructure(rows);
        List<Series> series = new ArrayList<>();
        for (Map.Entry<String, List<Row>> e : grouped.entrySet()) {
            Row first = e.getValue().get(0);
            series.add(new Series(label(first, false), first.structure, first.variant, null,
                    e.getValue(), color(first.structure), first.structure.equals("MyLinkedList")));
        }
        drawSeries(g, series, left, right, top, bottom, scale, true);
        drawLegendAndNotes(g, series, rows, x0 + width - 165, y0 + 62);
    }

    static void drawOperationsPanel(Graphics2D g, List<Row> rows, int x0, int y0, int width, int height) {
        int left = x0 + LEFT;
        int right = x0 + width - 175;
        int top = y0 + TOP;
        int bottom = y0 + height - BOTTOM;
        Scale scale = operationScale(rows);
        axes(g, left, right, top, bottom, scale, "operation count");

        List<Series> series = new ArrayList<>();
        Map<String, List<Row>> grouped = groupByStructure(rows);
        for (Map.Entry<String, List<Row>> e : grouped.entrySet()) {
            Row first = e.getValue().get(0);
            for (Metric metric : Metric.values()) {
                if (allZero(e.getValue(), metric)) continue;
                series.add(new Series(label(first, true) + " · " + metricLabel(metric),
                        first.structure, first.variant, metric, e.getValue(), color(first.structure),
                        first.structure.equals("MyLinkedList")));
            }
        }
        drawSeries(g, series, left, right, top, bottom, scale, false);
        drawLegendAndNotes(g, series, rows, x0 + width - 165, y0 + 62);
    }

    static Map<String, List<Row>> groupByStructure(List<Row> rows) {
        Map<String, List<Row>> result = new LinkedHashMap<>();
        rows.stream().sorted(Comparator.comparingInt(r -> r.n)).forEach(r ->
                result.computeIfAbsent(r.structure, k -> new ArrayList<>()).add(r));
        return result;
    }

    static List<Row> normalizeSeriesRows(List<Row> rows, String structure, String variant) {
        return rows.stream().filter(r -> r.structure.equals(structure) && r.variant.equals(variant))
                .sorted(Comparator.comparingInt(r -> r.n)).toList();
    }

    static void drawSeries(Graphics2D g, List<Series> series, int left, int right, int top, int bottom,
                           Scale scale, boolean time) {
        for (Series s : series) {
            List<Row> data = s.rows;
            g.setColor(s.color);
            g.setStroke(stroke(s.dashed));
            for (int i = 1; i < data.size(); i++) {
                Row a = data.get(i - 1), b = data.get(i);
                double av = time ? a.time : value(a, s.metric);
                double bv = time ? b.time : value(b, s.metric);
                if (av <= 0 || bv <= 0) continue;
                g.drawLine(px(a.n, left, right), py(av, scale, top, bottom),
                        px(b.n, left, right), py(bv, scale, top, bottom));
            }
            for (Row r : data) {
                double v = time ? r.time : value(r, s.metric);
                if (v <= 0) continue;
                drawMarker(g, px(r.n, left, right), py(v, scale, top, bottom), s.color,
                        time ? Metric.STEPS : s.metric);
            }
            if (time && !data.isEmpty()) {
                Row last = data.get(data.size() - 1);
                double v = last.time;
                if (v > 0) {
                    int lx = px(last.n, left, right) + 12;
                    int ly = py(v, scale, top, bottom) + 4;
                    g.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g.setColor(s.color);
                    g.drawString(label(last, false), Math.min(lx, right + 8), ly);
                }
            }
        }
    }

    static void drawLegendAndNotes(Graphics2D g, List<Series> series, List<Row> rows, int x, int y) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int yy = y;
        String[] structureNames = {"DynamicArray", "MyLinkedList", "MinHeap"};
        for (String name : structureNames) {
            boolean present = rows.stream().anyMatch(r -> r.structure.equals(name));
            if (!present) continue;
            g.setColor(color(name));
            g.setStroke(stroke(name.equals("MyLinkedList")));
            g.drawLine(x, yy, x + 28, yy);
            g.setColor(AXIS);
            g.drawString(name, x + 38, yy + 4);
            yy += 23;
        }
        if (series.stream().anyMatch(s -> s.metric != null)) {
            yy += 8;
            for (Metric metric : Metric.values()) {
                boolean present = series.stream().anyMatch(s -> s.metric == metric);
                if (!present) continue;
                g.setColor(AXIS);
                drawMarker(g, x + 12, yy, AXIS, metric);
                g.drawString(metricLabel(metric), x + 38, yy + 4);
                yy += 23;
            }
            for (Metric metric : Metric.values()) {
                boolean allZero = rows.stream().allMatch(r -> value(r, metric) == 0);
                if (allZero) {
                    g.setColor(AXIS);
                    g.drawString(metricLabel(metric) + " = 0 (not drawn)", x, yy + 4);
                    yy += 20;
                }
            }
        }
    }

    static Scale timeScale(List<Row> rows) {
        double min = Double.POSITIVE_INFINITY, max = 0;
        for (Row r : rows) {
            if (r.time > 0) { min = Math.min(min, r.time); max = Math.max(max, r.time); }
        }
        return scale(min, max);
    }

    static Scale operationScale(List<Row> rows) {
        double min = Double.POSITIVE_INFINITY, max = 0;
        for (Row r : rows) {
            for (Metric m : Metric.values()) {
                double v = value(r, m);
                if (v > 0) { min = Math.min(min, v); max = Math.max(max, v); }
            }
        }
        return scale(min, max);
    }

    static Scale scale(double min, double max) {
        if (!Double.isFinite(min) || max <= 0) return new Scale(1, 10);
        double lo = Math.floor(Math.log10(min));
        double hi = Math.ceil(Math.log10(max));
        if (lo == hi) { lo -= 1; hi += 1; }
        return new Scale(Math.pow(10, lo), Math.pow(10, hi));
    }

    static void axes(Graphics2D g, int left, int right, int top, int bottom, Scale scale, String yLabel) {
        g.setColor(GRID);
        for (int n : X_TICKS) {
            int xx = px(n, left, right);
            g.drawLine(xx, top, xx, bottom);
            g.setColor(AXIS);
            g.drawLine(xx, bottom, xx, bottom + 5);
            String text = n < 1000 ? Integer.toString(n) : "10^" + (int)Math.round(Math.log10(n));
            int tw = g.getFontMetrics().stringWidth(text);
            g.drawString(text, xx - tw / 2, bottom + 23);
            g.setColor(GRID);
        }
        int minPower = (int)Math.floor(Math.log10(scale.min));
        int maxPower = (int)Math.ceil(Math.log10(scale.max));
        for (int p = minPower; p <= maxPower; p++) {
            double v = Math.pow(10, p);
            int yy = py(v, scale, top, bottom);
            g.setColor(GRID); g.drawLine(left, yy, right, yy);
            g.setColor(AXIS); g.drawLine(left - 5, yy, left, yy);
            String text = formatTick(v);
            int tw = g.getFontMetrics().stringWidth(text);
            g.drawString(text, left - tw - 10, yy + 4);
        }
        g.setColor(AXIS);
        g.drawLine(left, bottom, right, bottom);
        g.drawLine(left, top, left, bottom);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("log-log", right - 35, top - 12);
    }

    static int px(double n, int left, int right) {
        double a = Math.log10(100), b = Math.log10(100_000);
        return left + (int)Math.round((Math.log10(n) - a) / (b - a) * (right - left));
    }

    static int py(double value, Scale scale, int top, int bottom) {
        double v = Math.log10(Math.max(value, scale.min));
        double lo = Math.log10(scale.min), hi = Math.log10(scale.max);
        return bottom - (int)Math.round((v - lo) / (hi - lo) * (bottom - top));
    }

    static double value(Row r, Metric metric) {
        return switch (metric) {
            case STEPS -> r.steps;
            case MOVES -> r.moves;
            case COMPARISONS -> r.comparisons;
        };
    }

    static boolean allZero(List<Row> rows, Metric metric) {
        return rows.stream().allMatch(r -> value(r, metric) == 0);
    }

    static Color color(String structure) {
        return switch (structure) {
            case "DynamicArray" -> ARRAY;
            case "MyLinkedList" -> LIST;
            case "MinHeap" -> HEAP;
            default -> AXIS;
        };
    }

    static BasicStroke stroke(boolean dashed) {
        return dashed ? new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f, new float[]{9f, 7f}, 0f) : new BasicStroke(2.8f);
    }

    static void drawMarker(Graphics2D g, int x, int y, Color c, Metric metric) {
        g.setColor(c);
        int s = 7;
        switch (metric) {
            case STEPS -> g.draw(new Ellipse2D.Double(x - s, y - s, 2*s, 2*s));
            case MOVES -> g.draw(new Rectangle2D.Double(x - s, y - s, 2*s, 2*s));
            case COMPARISONS -> {
                java.awt.Polygon p = new java.awt.Polygon();
                p.addPoint(x, y - s - 1); p.addPoint(x - s - 1, y + s); p.addPoint(x + s + 1, y + s);
                g.drawPolygon(p);
            }
        }
    }

    static String metricLabel(Metric metric) {
        return switch (metric) {
            case STEPS -> "steps ○";
            case MOVES -> "moves □";
            case COMPARISONS -> "comparisons △";
        };
    }

    static String label(Row r, boolean includeVariant) {
        if (includeVariant && !r.variant.equals("-")) return r.structure + " " + r.variant;
        return r.structure;
    }

    static String formatTick(double value) {
        if (value >= 1_000_000_000) return String.format("%.0fB", value / 1_000_000_000);
        if (value >= 1_000_000) return String.format("%.0fM", value / 1_000_000);
        if (value >= 1_000) return String.format("%.0fk", value / 1_000);
        if (value >= 1) return String.format("%.0f", value);
        if (value >= 0.001) return String.format("%.3f", value);
        return String.format("%.1e", value);
    }

    static final class BufferedImageBuilder {
        final java.awt.image.BufferedImage image;
        final Graphics2D g;
        BufferedImageBuilder(int width, int height) {
            image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
        }
        void dispose() { g.dispose(); }
    }
}
