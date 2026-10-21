package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AutomobileFileReader}.
 *
 * <p>Tests for {@code TODO(validation)} in {@code parseLine} (field count,
 * non-blank model, power &gt; 0, plausible year) are intentionally omitted
 * until that work lands
 */
class AutomobileFileReaderTest {

    @TempDir
    Path tmp;

    // ------------------------------------------------------------------
    // Parsing — happy path
    // ------------------------------------------------------------------

    @Test
    void readsSingleRecord() throws IOException {
        CustomList<Automobile> result =
                AutomobileFileReader.readAll("BMW X5;340;2022");

        assertEquals(1, result.size());
        Automobile a = result.get(0);
        assertEquals("BMW X5", a.getModel());
        assertEquals(340,      a.getPower());
        assertEquals(2022,     a.getYear());
    }

    @Test
    void readsMultipleRecords() throws IOException {
        String content = String.join("\n",
                "BMW X5;340;2022",
                "Toyota Camry;250;2020",
                "Lada 2107;75;1985");

        CustomList<Automobile> result = AutomobileFileReader.readAll(content);

        assertEquals(3, result.size());
        assertEquals("BMW X5",       result.get(0).getModel());
        assertEquals("Toyota Camry", result.get(1).getModel());
        assertEquals("Lada 2107",    result.get(2).getModel());
    }

    @Test
    void handlesCrLfLineEndings() throws IOException {
        CustomList<Automobile> result =
                AutomobileFileReader.readAll("BMW X5;340;2022\r\nAudi A6;250;2021\r\n");

        assertEquals(2, result.size());
        assertEquals("BMW X5",  result.get(0).getModel());
        assertEquals("Audi A6", result.get(1).getModel());
    }

    @Test
    void trimsWhitespaceAroundFields() throws IOException {
        CustomList<Automobile> result =
                AutomobileFileReader.readAll("   BMW X5  ;  340 ; 2022 ");

        assertEquals(1, result.size());
        Automobile a = result.get(0);
        assertEquals("BMW X5", a.getModel());
        assertEquals(340,      a.getPower());
        assertEquals(2022,     a.getYear());
    }

    // ------------------------------------------------------------------
    // Comments, blank lines, BOM
    // ------------------------------------------------------------------

    @Test
    void skipsCommentsAndBlankLines() throws IOException {
        String content = String.join("\n",
                "# this is a comment",
                "",
                "BMW X5;340;2022",
                "   ",
                "# trailing comment",
                "Audi A6;250;2021");

        CustomList<Automobile> result = AutomobileFileReader.readAll(content);

        assertEquals(2, result.size());
        assertEquals("BMW X5",  result.get(0).getModel());
        assertEquals("Audi A6", result.get(1).getModel());
    }

    @Test
    void returnsEmptyListForEmptyInput() throws IOException {
        assertEquals(0, AutomobileFileReader.readAll("").size());
    }

    @Test
    void returnsEmptyListForOnlyCommentsAndBlanks() throws IOException {
        assertEquals(0, AutomobileFileReader.readAll(
                "# nothing but a header\n\n   \n# and another comment\n").size());
    }

    @Test
    void stripsLeadingBomOnFirstLine() throws IOException {
        CustomList<Automobile> result =
                AutomobileFileReader.readAll("\uFEFFBMW X5;340;2022");

        assertEquals(1, result.size());
        assertEquals("BMW X5", result.get(0).getModel());
    }

    @Test
    void stripsBomWhenFirstLineIsComment() throws IOException {
        // Ordering check: BOM must be stripped *before* the '#' check.
        // If someone reorders them, the first line becomes "\uFEFF# model..."
        // which is no longer recognized as a comment and blows up in parseLine.
        String content = "\uFEFF# model;power;year\nBMW X5;340;2022";

        CustomList<Automobile> result = AutomobileFileReader.readAll(content);

        assertEquals(1, result.size());
        assertEquals("BMW X5", result.get(0).getModel());
    }

    // ------------------------------------------------------------------
    // Encoding — must fail on any non-UTF-8 decode
    // ------------------------------------------------------------------

    @Test
    void readsNonAsciiFromUtf8File() throws IOException {
        // Pure-ASCII fixtures pass under cp1252, ISO-8859-1 and UTF-8 alike,
        // so they can't verify the UTF-8 contract. Non-ASCII content can.
        Path file = tmp.resolve("cars.txt");
        Files.writeString(file, "Citroën C5;150;2020", StandardCharsets.UTF_8);

        CustomList<Automobile> result = AutomobileFileReader.readAll(file);

        assertEquals(1, result.size());
        assertEquals("Citroën C5", result.get(0).getModel());
    }

    @Test
    void readsNonAsciiFromUtf8String() throws IOException {
        CustomList<Automobile> result =
                AutomobileFileReader.readAll("Škoda Octavia;150;2021");

        assertEquals(1, result.size());
        assertEquals("Škoda Octavia", result.get(0).getModel());
    }

    // ------------------------------------------------------------------
    // Error reporting
    // ------------------------------------------------------------------

    @Test
    void wrapsBadNumberWithLineNumberAndOriginalLine() {
        String content = "# header\nBMW X5;abc;2022";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AutomobileFileReader.readAll(content));

        assertTrue(ex.getMessage().contains("Line 2"),
                () -> "message should mention line number, was: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("BMW X5;abc;2022"),
                () -> "message should include original line, was: " + ex.getMessage());
        assertTrue(ex.getCause() instanceof NumberFormatException,
                () -> "cause should be NumberFormatException, was: " + ex.getCause());
    }

    @Test
    void throwsIOExceptionOnMissingFile() {
        assertThrows(IOException.class,
                () -> AutomobileFileReader.readAll(tmp.resolve("does-not-exist.txt")));
    }

    // ------------------------------------------------------------------
    // Resource ownership — caller owns the Reader, reader must not close it
    // ------------------------------------------------------------------

    @Test
    void doesNotCloseCallerProvidedReader() throws IOException {
        TrackingReader reader = new TrackingReader("BMW X5;340;2022");

        AutomobileFileReader.readAll(reader);

        assertFalse(reader.closed,
                "readAll(Reader) must not close a caller-owned Reader");
    }

    @Test
    void doesNotCloseCallerProvidedBufferedReader() throws IOException {
        // Exercises the "instanceof BufferedReader" fast path — a distinct
        // code branch from the plain-Reader path above.
        TrackingBufferedReader reader =
                new TrackingBufferedReader("BMW X5;340;2022");

        AutomobileFileReader.readAll(reader);

        assertFalse(reader.closed,
                "readAll(Reader) must not close a caller-owned BufferedReader");
    }

    // ------------------------------------------------------------------
    // Tracking helpers
    // ------------------------------------------------------------------

    private static final class TrackingReader extends StringReader {
        boolean closed;

        TrackingReader(String content) {
            super(content);
        }

        @Override
        public void close() {
            closed = true;
            super.close();
        }
    }

    private static final class TrackingBufferedReader extends BufferedReader {
        boolean closed;

        TrackingBufferedReader(String content) {
            super(new StringReader(content));
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
