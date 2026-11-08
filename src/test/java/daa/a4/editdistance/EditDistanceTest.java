package daa.a4.editdistance;

import daa.a4.experiment.InputGenerator;
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
    void referenceValues(EditDistance ed) {
        assertEquals(3, ed.distance("kitten", "sitting"));
        assertEquals(2, ed.distance("flaw", "lawn"));
        assertEquals(5, ed.distance("intention", "execution"));
        assertEquals(3, ed.distance("", "abc"));
        assertEquals(0, ed.distance("abc", "abc"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
    void emptyStrings(EditDistance ed) {
        assertEquals(0, ed.distance("", ""));
        assertEquals(3, ed.distance("abc", ""));
        assertEquals(5, ed.distance("", "hello"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
    void streetTypo(EditDistance ed) {
        assertEquals(2, ed.distance("Abylai Han", "Abylai Khan"));
        assertEquals(1, ed.distance("Abylai Kan", "Abylai Khan"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versions")
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
    void allThreeAgreeOnRandomStrings() {
        Random r = new Random(42);
        List<EditDistance> eds = List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());
        for (int t = 0; t < 100; t++) {
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
