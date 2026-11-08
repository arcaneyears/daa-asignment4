package daa.a4.experiment;

import java.util.Random;

/**
 * Reproducible input generator. Every input is built from a Random whose seed depends only on
 * the base seed, the input type and the size, so the same command always gives the same inputs
 * (and the same counters), no matter in which order the experiment runs them.
 */
public final class InputGenerator {

    public static final long DEFAULT_SEED = 20261112L;

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String FIRST_HALF = "abcdefghijklm";
    private static final String SECOND_HALF = "nopqrstuvwxyz";

    private static final String[] LOG_LINES = {
            "INFO  200 GET /api/streets?q=Abylai+Khan\n",
            "INFO  200 GET /api/streets?q=Kenesary\n",
            "INFO  200 GET /api/streets?q=Turan\n",
            "WARN  429 GET /api/search rate limit\n",
            "INFO  304 GET /static/map.js\n",
            "INFO  200 POST /api/feedback\n",
            "ERROR 503 /api/streets upstream timeout\n",
            "DEBUG cache miss key=street:1043\n",
    };
    /** The error pattern operators search for in the logs. */
    public static final String LOG_PATTERN = "ERROR 503 /api/streets";

    private final long baseSeed;

    public InputGenerator(long baseSeed) {
        this.baseSeed = baseSeed;
    }

    /** A fresh Random for one (input type, size) pair. */
    public Random rng(String inputType, int n) {
        long seed = baseSeed;
        seed = seed * 31 + inputType.hashCode();
        seed = seed * 31 + n;
        return new Random(seed);
    }

    public static String randomString(Random r, int n, String alphabet) {
        char[] c = new char[n];
        for (int i = 0; i < n; i++) c[i] = alphabet.charAt(r.nextInt(alphabet.length()));
        return new String(c);
    }

    public static String repeat(char ch, int n) {
        return String.valueOf(ch).repeat(n);
    }

    // ---------- Edit Distance inputs: a pair of strings of length n ----------

    /** identical: a == b. Best case for the recursion: every step matches, one call per level. */
    public String[] edIdentical(int n) {
        String a = randomString(rng("identical", n), n, LOWER);
        return new String[]{a, a};
    }

    /** typo: b is a copy of a with about 10% random replacements, like "Abylai Han" vs "Abylai Khan". */
    public String[] edTypo(int n) {
        Random r = rng("typo", n);
        String a = randomString(r, n, LOWER);
        char[] b = a.toCharArray();
        int edits = Math.max(1, n / 10);
        for (int k = 0; k < edits; k++) {
            int pos = r.nextInt(n);
            char old = b[pos];
            char c;
            do {
                c = LOWER.charAt(r.nextInt(LOWER.length()));
            } while (c == old);
            b[pos] = c;
        }
        return new String[]{a, new String(b)};
    }

    /** disjoint: a and b share no character. Worst case for the recursion: every call branches 3 ways. */
    public String[] edDisjoint(int n) {
        Random r = rng("disjoint", n);
        return new String[]{randomString(r, n, FIRST_HALF), randomString(r, n, SECOND_HALF)};
    }

    public String[] edInput(String type, int n) {
        return switch (type) {
            case "identical" -> edIdentical(n);
            case "typo" -> edTypo(n);
            case "disjoint" -> edDisjoint(n);
            default -> throw new IllegalArgumentException("Unknown input " + type);
        };
    }

    // ---------- String search inputs: {text, pattern} ----------

    /** random: random lowercase text and a random pattern of length 10. Mismatches come early. */
    public String[] searchRandom(int n) {
        Random r = rng("random", n);
        return new String[]{randomString(r, n, LOWER), randomString(r, 10, LOWER)};
    }

    /** worst: text "AAA...A", pattern "AA...AB" (length m). Naive compares m characters at every position. */
    public String[] searchWorst(int n, int m) {
        return new String[]{repeat('A', n), repeat('A', m - 1) + "B"};
    }

    /** logs: realistic log file built from typical lines; pattern is the 503 error line. */
    public String[] searchLogs(int n) {
        Random r = rng("logs", n);
        StringBuilder sb = new StringBuilder(n + 64);
        while (sb.length() < n) {
            // errors are rare: about 1 in 40 lines
            String line = r.nextInt(40) == 0 ? LOG_LINES[6] : LOG_LINES[r.nextInt(LOG_LINES.length)];
            sb.append(line);
        }
        sb.setLength(n);
        return new String[]{sb.toString(), LOG_PATTERN};
    }
}
