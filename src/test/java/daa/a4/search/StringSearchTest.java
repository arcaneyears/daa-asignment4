package daa.a4.search;

import daa.a4.experiment.InputGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringSearchTest {

    static Stream<StringSearch> searches() {
        return Stream.of(new NaiveSearch(), new KmpSearch());
    }

    // ---------------- LPS ----------------

    @Test
    @DisplayName("LPS reference arrays from section 7")
    void lpsReference() {
        KmpSearch kmp = new KmpSearch();
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, kmp.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, kmp.lps("ABABCABAB"));
    }

    @Test
    void lpsSmallCases() {
        KmpSearch kmp = new KmpSearch();
        assertArrayEquals(new int[]{0}, kmp.lps("A"));
        assertArrayEquals(new int[]{0, 1, 2, 3}, kmp.lps("AAAA"));
        assertArrayEquals(new int[]{0, 0, 0, 0}, kmp.lps("ABCD"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 3, 4}, kmp.lps("ABABAB"));
    }

    // ---------------- reference matches ----------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("searches")
    @DisplayName("Reference matches from section 7 (incl. overlapping)")
    void referenceMatches(StringSearch s) {
        assertEquals(List.of(10), s.search("ABABDABACDABABCABAB", "ABABCABAB"));
        assertEquals(List.of(0, 1, 2, 3), s.search("AAAAA", "AA"));
        assertEquals(List.of(0, 2, 4), s.search("ABABABA", "ABA"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searches")
    void otherCases(StringSearch s) {
        assertEquals(List.of(), s.search("ABCDEF", "XYZ"));
        assertEquals(List.of(0), s.search("ABC", "ABC"));
        assertEquals(List.of(0, 1, 2), s.search("AAA", "A"));
        assertEquals(List.of(4), s.search("abc abylai khan", "abylai"));
    }

    // ---------------- edge cases ----------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("searches")
    @DisplayName("Empty pattern throws IllegalArgumentException")
    void emptyPatternThrows(StringSearch s) {
        assertThrows(IllegalArgumentException.class, () -> s.search("ABC", ""));
        assertThrows(IllegalArgumentException.class, () -> s.search("", ""));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searches")
    @DisplayName("Pattern longer than the text returns an empty list")
    void patternLongerThanText(StringSearch s) {
        assertTrue(s.search("AB", "ABC").isEmpty());
        assertTrue(s.search("", "A").isEmpty());
    }

    // ---------------- cross-check ----------------

    @Test
    @DisplayName("100 random texts: Naive and KMP return the same list")
    void naiveAndKmpAgree() {
        Random r = new Random(7);
        for (int t = 0; t < 100; t++) {
            // small alphabet => many (overlapping) matches
            String text = InputGenerator.randomString(r, r.nextInt(200), "ab");
            String pattern = InputGenerator.randomString(r, 1 + r.nextInt(6), "ab");
            assertEquals(new NaiveSearch().search(text, pattern), new KmpSearch().search(text, pattern),
                    "text=" + text + " pattern=" + pattern);
        }
    }

    @Test
    @DisplayName("KMP never compares more than 2n characters in the search phase")
    void kmpLinearComparisons() {
        String text = "A".repeat(10_000);
        String pattern = "A".repeat(99) + "B";
        KmpSearch kmp = new KmpSearch();
        NaiveSearch naive = new NaiveSearch();
        kmp.search(text, pattern);
        naive.search(text, pattern);
        assertTrue(kmp.counters().charComparisons <= 2L * text.length());
        assertEquals((long) (text.length() - pattern.length() + 1) * pattern.length(),
                naive.counters().charComparisons);
    }
}
