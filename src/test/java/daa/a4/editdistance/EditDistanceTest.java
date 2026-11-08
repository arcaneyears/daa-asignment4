package daa.a4.editdistance;

import daa.a4.experiment.InputGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditDistanceTest {

    static Stream<EditDistance> versions() {
        return Stream.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
    @DisplayName("Reference values from section 7")
    void referenceValues(EditDistance ed) {
        assertEquals(3, ed.distance("kitten", "sitting"));
        assertEquals(2, ed.distance("flaw", "lawn"));
        assertEquals(5, ed.distance("intention", "execution"));
        assertEquals(3, ed.distance("", "abc"));
        assertEquals(0, ed.distance("abc", "abc"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
    @DisplayName("Empty strings")
    void emptyStrings(EditDistance ed) {
        assertEquals(0, ed.distance("", ""));
        assertEquals(3, ed.distance("abc", ""));
        assertEquals(5, ed.distance("", "hello"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
    @DisplayName("Scenario: street name with a typo")
    void streetTypo(EditDistance ed) {
        // "Han" -> "Khan": replace H with K, insert h
        assertEquals(2, ed.distance("Abylai Han", "Abylai Khan"));
        assertEquals(1, ed.distance("Abylai Kan", "Abylai Khan"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
    @DisplayName("Symmetry and simple bounds")
    void symmetryAndBounds(EditDistance ed) {
        assertEquals(ed.distance("sunday", "saturday"), ed.distance("saturday", "sunday"));
        assertEquals(3, ed.distance("sunday", "saturday"));
        assertEquals(4, ed.distance("abcd", "wxyz"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
    void nullThrows(EditDistance ed) {
        assertThrows(IllegalArgumentException.class, () -> ed.distance(null, "a"));
    }

    @Test
    @DisplayName("100 random short strings: all three versions give the same number")
    void allThreeAgreeOnRandomStrings() {
        Random r = new Random(42);
        List<EditDistance> eds = List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());
        for (int t = 0; t < 100; t++) {
            // small alphabet so that equal characters actually happen; length 0..8 keeps recursion fast
            String a = InputGenerator.randomString(r, r.nextInt(9), "abc");
            String b = InputGenerator.randomString(r, r.nextInt(9), "abc");
            int expected = eds.get(1).distance(a, b);
            for (EditDistance ed : eds) {
                assertEquals(expected, ed.distance(a, b), ed.name() + " on \"" + a + "\" vs \"" + b + "\"");
            }
            assertTrue(expected <= Math.max(a.length(), b.length()));
            assertTrue(expected >= Math.abs(a.length() - b.length()));
        }
    }

    @Test
    @DisplayName("Counters: table and two rows do the same work but use different memory")
    void countersAreCountedInside() {
        TableEditDistance table = new TableEditDistance();
        TwoRowEditDistance rows = new TwoRowEditDistance();
        table.distance("kitten", "sitting");
        rows.distance("kitten", "sitting");
        assertEquals(6L * 7, table.counters().charComparisons);
        assertEquals(table.counters().charComparisons, rows.counters().charComparisons);
        assertEquals(7L * 8, table.counters().memoryCells);
        assertEquals(2L * 8, rows.counters().memoryCells);
    }
}
