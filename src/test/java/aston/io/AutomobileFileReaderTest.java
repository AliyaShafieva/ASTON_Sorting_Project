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
 * <p>
 * Tests for {@code TODO(validation)} in {@code parseLine} (field count,
 * non-blank model, power &gt; 0, plausible year) are intentionally omitted
 * until that work lands.
 */
class AutomobileFileReaderTest {

    @TempDir
    Path tmp;

    // ------------------------------------------------------------------
    // Parsing — happy path, CRLF, whitespace
    // ------------------------------------------------------------------

    @Test
    void parsesMultipleRecordsWithCrLfAndWhitespace() {
        String content = String.join("\r\n",
                "   BMW X5  ;  340 ; 2022 ",
                "Toyota Camry;250;2020",
                "Lada 2107;75;1985");

        CustomList<Automobile> result = AutomobileFileReader.parse(content);

        assertEquals(3, result.size());
        assertAuto(result.get(0), "BMW X5", 340, 2022);
        assertAuto(result.get(1), "Toyota Camry", 250, 2020);
        assertAuto(result.get(2), "Lada 2107", 75, 1985);
    }

    // ------------------------------------------------------------------
    // Skipping — comments, blanks, empty
    // ------------------------------------------------------------------

    @Test
    void skipsCommentsAndBlankLines() {
        String content = String.join("\n",
                "# this is a comment",
                "",
                "BMW X5;340;2022",
                "   ",
                "# trailing comment",
                "Audi A6;250;2021");

        CustomList<Automobile> result = AutomobileFileReader.parse(content);

        assertEquals(2, result.size());
        assertEquals("BMW X5", result.get(0).getModel());
        assertEquals("Audi A6", result.get(1).getModel());
    }

    @Test
    void returnsEmptyListWhenNothingToParse() {
        assertEquals(0, AutomobileFileReader.parse("").size());
        assertEquals(0, AutomobileFileReader.parse("# header only\n\n   \n").size());
    }

    // ------------------------------------------------------------------
    // BOM
    // ------------------------------------------------------------------

    @Test
    void stripsBomBeforeCommentCheck() {
        CustomList<Automobile> plain = AutomobileFileReader.parse("\uFEFFBMW X5;340;2022");
        CustomList<Automobile> comment = AutomobileFileReader.parse("\uFEFF# header\nBMW X5;340;2022");

        assertEquals(1, plain.size());
        assertEquals(1, comment.size());
        assertEquals("BMW X5", plain.get(0).getModel());
        assertEquals("BMW X5", comment.get(0).getModel());
    }

    // ------------------------------------------------------------------
    // Encoding — UTF-8 on file read
    // ------------------------------------------------------------------

    @Test
    void readsUtf8File() throws IOException {
        // Pure-ASCII fixtures pass under any single-byte charset;
        // only non-ASCII content verifies the UTF-8 contract.
        Path file = tmp.resolve("cars.txt");
        Files.writeString(file, "Citroën C5;150;2020", StandardCharsets.UTF_8);

        CustomList<Automobile> result = AutomobileFileReader.readAll(file);

        assertEquals(1, result.size());
        assertEquals("Citroën C5", result.get(0).getModel());
    }

    // ------------------------------------------------------------------
    // Error reporting & contracts
    // ------------------------------------------------------------------

    @Test
    void wrapsBadNumberWithLineNumberAndOriginalLine() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AutomobileFileReader.parse("# header\nBMW X5;abc;2022"));

        assertTrue(ex.getMessage().contains("Line 2"), ex::getMessage);
        assertTrue(ex.getMessage().contains("BMW X5;abc;2022"), ex::getMessage);
        assertTrue(ex.getCause() instanceof NumberFormatException, ex::getMessage);
    }

    @Test
    void throwsIOExceptionOnMissingFile() {
        assertThrows(IOException.class,
                () -> AutomobileFileReader.readAll(tmp.resolve("missing.txt")));
    }

    @Test
    void parseRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> AutomobileFileReader.parse(null));
    }

    // ------------------------------------------------------------------
    // Resource ownership — caller owns the Reader
    // ------------------------------------------------------------------

    @Test
    void doesNotCloseCallerProvidedReader() throws IOException {
        TrackingReader plain = new TrackingReader("BMW X5;340;2022");
        TrackingBufferedReader buffered = new TrackingBufferedReader("BMW X5;340;2022");

        AutomobileFileReader.readAll(plain);
        AutomobileFileReader.readAll(buffered);

        assertFalse(plain.closed, "plain Reader must not be closed");
        assertFalse(buffered.closed, "BufferedReader must not be closed (fast path)");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void assertAuto(Automobile a, String model, int power, int year) {
        assertEquals(model, a.getModel());
        assertEquals(power, a.getPower());
        assertEquals(year, a.getYear());
    }

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
