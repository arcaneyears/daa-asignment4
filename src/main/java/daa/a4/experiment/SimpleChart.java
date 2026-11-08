package daa.a4.experiment;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A tiny XY line chart drawn with java.awt and saved as PNG.
 * Supports a title, labeled axes, a legend, markers, dashed lines and log axes.
 * Written for this project so the experiment needs no chart library.
 */
public final class SimpleChart {

    private static final Color[] PALETTE = {
            new Color(0x1f77b4), new Color(0xd62728), new Color(0x2ca02c),
            new Color(0xff7f0e), new Color(0x9467bd), new Color(0x8c564b)};

    private record Series(String name, double[] x, double[] y, Color color, boolean dashed, int marker) {
    }

    private final String title;
    private final String xTitle;
    private final String yTitle;
    private final boolean logX;
    private final boolean logY;
    private final List<Series> series = new ArrayList<>();

    private final int width = 1200;
    private final int height = 750;
    private final int left = 120;
    private final int right = 40;
    private final int top = 70;
    private final int bottom = 90;

    public SimpleChart(String title, String xTitle, String yTitle, boolean logX, boolean logY) {
        this.title = title;
        this.xTitle = xTitle;
        this.yTitle = yTitle;
        this.logX = logX;
        this.logY = logY;
    }

    public void addSeries(String name, double[] x, double[] y, boolean dashed) {
        int i = series.size();
        series.add(new Series(name, x, y, PALETTE[i % PALETTE.length], dashed, i % 4));
    }

    public boolean isEmpty() {
        return series.isEmpty();
    }

    // ------------------------------------------------------------------ scales

    private double tx(double v, boolean log) {
        return log ? Math.log10(v) : v;
    }

    private double[] range(boolean xAxis, boolean log) {
        double lo = Double.POSITIVE_INFINITY;
        double hi = Double.NEGATIVE_INFINITY;
        for (Series s : series) {
            for (double v : xAxis ? s.x() : s.y()) {
                if (log && v <= 0) continue;
                lo = Math.min(lo, v);
                hi = Math.max(hi, v);
            }
        }
        if (log) {
            double a = Math.floor(Math.log10(lo));
            double b = Math.ceil(Math.log10(hi * 1.6));
            if (a == b) b = a + 1;
            return new double[]{a, b};
        }
        if (!xAxis) lo = Math.min(lo, 0);   // linear y starts at 0 so ratios are honest
        if (lo == hi) hi = lo + 1;
        double step = niceStep((hi - lo) / 6);
        return new double[]{Math.floor(lo / step) * step, Math.ceil(hi / step) * step};
    }

    private static double niceStep(double raw) {
        double p = Math.pow(10, Math.floor(Math.log10(raw)));
        double f = raw / p;
        double nice = f <= 1 ? 1 : f <= 2 ? 2 : f <= 5 ? 5 : 10;
        return nice * p;
    }

    private List<Double> ticks(double[] r, boolean log) {
        List<Double> t = new ArrayList<>();
        if (log) {
            boolean fine = r[1] - r[0] <= 3;
            for (int e = (int) r[0]; e <= (int) r[1]; e++) {
                t.add((double) e);
                if (fine && e < (int) r[1]) {
                    t.add(e + Math.log10(2));
                    t.add(e + Math.log10(5));
                }
            }
        } else {
            double step = niceStep((r[1] - r[0]) / 6);
            for (double v = r[0]; v <= r[1] + step / 2; v += step) t.add(v);
        }
        return t;
    }

    private static String label(double v, boolean log) {
        if (log) {
            double real = Math.pow(10, v);
            if (v >= -1e-9 && v <= 6.5) return String.format(Locale.ROOT, "%,d", Math.round(real));
            if (v < 0 && v >= -3) return String.format(Locale.ROOT, "%." + (int) Math.ceil(-v - 1e-9) + "f", real);
            double mant = real / Math.pow(10, Math.floor(v + 1e-9));
            return String.format(Locale.ROOT, "%de%d", Math.round(mant), (int) Math.floor(v + 1e-9));
        }
        if (Math.abs(v) >= 1e6) return String.format(Locale.ROOT, "%.1e", v);
        if (v == Math.rint(v)) return String.format(Locale.ROOT, "%,d", (long) v);
        return String.format(Locale.ROOT, "%.2f", v);
    }

    // ------------------------------------------------------------------ drawing

    public void save(Path file) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int pw = width - left - right;
        int ph = height - top - bottom;
        double[] xr = range(true, logX);
        double[] yr = range(false, logY);

        // title
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, 42);

        // grid + tick labels
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        fm = g.getFontMetrics();
        for (double t : ticks(xr, logX)) {
            int px = left + (int) Math.round((t - xr[0]) / (xr[1] - xr[0]) * pw);
            g.setColor(new Color(0xe3e3e3));
            g.drawLine(px, top, px, top + ph);
            g.setColor(Color.DARK_GRAY);
            String s = label(t, logX);
            g.drawString(s, px - fm.stringWidth(s) / 2, top + ph + 22);
        }
        for (double t : ticks(yr, logY)) {
            int py = top + ph - (int) Math.round((t - yr[0]) / (yr[1] - yr[0]) * ph);
            g.setColor(new Color(0xe3e3e3));
            g.drawLine(left, py, left + pw, py);
            g.setColor(Color.DARK_GRAY);
            String s = label(t, logY);
            g.drawString(s, left - 10 - fm.stringWidth(s), py + 5);
        }
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(left, top, pw, ph);

        // axis titles
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        fm = g.getFontMetrics();
        g.drawString(xTitle, left + (pw - fm.stringWidth(xTitle)) / 2, height - 30);
        AffineTransform old = g.getTransform();
        g.rotate(-Math.PI / 2);
        g.drawString(yTitle, -(top + (ph + fm.stringWidth(yTitle)) / 2), 30);
        g.setTransform(old);

        // series
        for (Series s : series) {
            g.setColor(s.color());
            Stroke stroke = s.dashed()
                    ? new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{10f, 6f}, 0f)
                    : new BasicStroke(2.5f);
            int prevX = -1;
            int prevY = -1;
            for (int i = 0; i < s.x().length; i++) {
                if ((logX && s.x()[i] <= 0) || (logY && s.y()[i] <= 0)) continue;
                int px = left + (int) Math.round((tx(s.x()[i], logX) - xr[0]) / (xr[1] - xr[0]) * pw);
                int py = top + ph - (int) Math.round((tx(s.y()[i], logY) - yr[0]) / (yr[1] - yr[0]) * ph);
                g.setStroke(stroke);
                if (prevX >= 0) g.drawLine(prevX, prevY, px, py);
                g.setStroke(new BasicStroke(2f));
                marker(g, s.marker(), px, py);
                prevX = px;
                prevY = py;
            }
        }

        // legend (top-left inside the plot)
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        fm = g.getFontMetrics();
        int lw = 0;
        for (Series s : series) lw = Math.max(lw, fm.stringWidth(s.name()));
        int lx = left + 14;
        int ly = top + 14;
        int lh = series.size() * 24 + 12;
        g.setColor(new Color(255, 255, 255, 235));
        g.fillRect(lx, ly, lw + 70, lh);
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(1f));
        g.drawRect(lx, ly, lw + 70, lh);
        for (int i = 0; i < series.size(); i++) {
            Series s = series.get(i);
            int y = ly + 22 + i * 24;
            g.setColor(s.color());
            g.setStroke(s.dashed()
                    ? new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 5f}, 0f)
                    : new BasicStroke(2.5f));
            g.drawLine(lx + 8, y - 5, lx + 44, y - 5);
            g.setStroke(new BasicStroke(2f));
            marker(g, s.marker(), lx + 26, y - 5);
            g.setColor(Color.BLACK);
            g.drawString(s.name(), lx + 54, y);
        }

        g.dispose();
        ImageIO.write(img, "png", file.toFile());
    }

    private static void marker(Graphics2D g, int type, int x, int y) {
        int r = 5;
        switch (type) {
            case 0 -> g.fillOval(x - r, y - r, 2 * r, 2 * r);
            case 1 -> g.fillRect(x - r, y - r, 2 * r, 2 * r);
            case 2 -> g.fillPolygon(new int[]{x, x + r + 1, x, x - r - 1}, new int[]{y - r - 1, y, y + r + 1, y}, 4);
            default -> g.fillPolygon(new int[]{x, x + r + 1, x - r - 1}, new int[]{y - r - 1, y + r, y + r}, 3);
        }
    }
}
