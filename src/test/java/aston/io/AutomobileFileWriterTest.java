package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AutomobileFileWriter}.
 *
 * <p>Round-trip tests use {@link AutomobileFileReader#parse(String)} and
 * {@link AutomobileFileReader#readAll(Path)} to assert the writer→reader
 * contract end-to-end, not just the byte format.
 *
 * <p>Tests for the reader's {@code TODO(validation)} block are intentionally
 * absent here — see {@code AutomobileFileReaderTest}.
 */
class AutomobileFileWriterTest {

    private static final String HEADER = "# model;power;year";

    // ---------------------------------------------------------------- helpers

    private static Automobile auto(String model, int power, int year) {
        return new Automobile.Builder()
                .model(model)
                .power(power)
                .year(year)
                .build();
    }

    private static CustomList<Automobile> list(Automobile... autos) {
        CustomList<Automobile> l = new CustomList<>();
        for (Automobile a : autos) {
            l.add(a);
        }
        return l;
    }

    private static List<String> readLines(Path p) throws IOException {
        return Files.readAllLines(p, StandardCharsets.UTF_8);
    }

    /** Runs writeAll(Writer, …) into a fresh StringWriter and returns its output. */
    private static String writeToString(CustomList<Automobile> autos) throws IOException {
        StringWriter sw = new StringWriter();
        AutomobileFileWriter.writeAll(sw, autos);
        return sw.toString();
    }

    private static String writeToString(Automobile... autos) throws IOException {
        return writeToString(list(autos));
    }

    /** Asserts writeAll(Writer, …) rejects the list with IllegalArgumentException. */
    private static void assertRejects(CustomList<Automobile> autos) {
        assertThrows(IllegalArgumentException.class,
                () -> AutomobileFileWriter.writeAll(new StringWriter(), autos));
    }

    private static void assertRejects(Automobile... autos) {
        assertRejects(list(autos));
    }

    /** Records flush/close calls — the writer must never trigger them on a caller-owned Writer. */
    private static final class TrackingWriter extends Writer {
        final StringBuilder sb = new StringBuilder();
        boolean flushed, closed;

        @Override public void write(char[] cbuf, int off, int len) { sb.append(cbuf, off, len); }
        @Override public void flush() { flushed = true; }
        @Override public void close() { closed = true; }
        @Override public String toString() { return sb.toString(); }
    }

    // =====================================================================
    // writeAll(Path, CustomList)
    // =====================================================================
    @Nested
    class WriteAllPath {

        @Test
        void createsFileWithHeaderAndRecords(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.writeAll(file,
                    list(auto("BMW X5", 340, 2022), auto("Toyota Camry", 250, 2020)));

            assertEquals(
                    List.of(HEADER, "BMW X5;340;2022", "Toyota Camry;250;2020"),
                    readLines(file));
        }

        @Test
        void emptyListWritesOnlyHeader(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.writeAll(file, list());
            assertEquals(List.of(HEADER), readLines(file));
        }

        @Test
        void overwritesExistingFile(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            Files.writeString(file, "stale\ncontent\n");
            AutomobileFileWriter.writeAll(file, list(auto("A", 1, 2)));
            assertEquals(List.of(HEADER, "A;1;2"), readLines(file));
        }

        /** Byte-exact: UTF-8 encoding, LF-only terminator, exactly one header line. */
        @Test
        void writesUtf8LfBytesWithSingleHeader(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.writeAll(file, list(
                    auto("Citroën C5", 120, 2019),
                    auto("BMW X5",     340, 2022)));

            byte[] expected = (
                    HEADER + "\n" +
                    "Citroën C5;120;2019\n" +
                    "BMW X5;340;2022\n"
            ).getBytes(StandardCharsets.UTF_8);

            assertArrayEquals(expected, Files.readAllBytes(file));
        }

        /** Validation must run before any file I/O — missing file not created, existing preserved. */
        @Test
        void validationFailureDoesNotTouchFile(@TempDir Path dir) throws IOException {
            Path missing = dir.resolve("missing.csv");
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(missing, list(auto("bad;model", 1, 2))));
            assertFalse(Files.exists(missing), "missing file must not be created");

            Path existing = dir.resolve("existing.csv");
            Files.writeString(existing, "original\n");
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(existing, list(auto("bad;model", 1, 2))));
            assertEquals("original\n", Files.readString(existing), "existing file must be untouched");
        }

        @Test
        void leavesNoTempFiles(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.writeAll(file, list(auto("A", 1, 2)));
            try (var stream = Files.list(dir)) {
                assertEquals(List.of(file), stream.toList());
            }
        }

        @Test
        void rejectsNullArguments(@TempDir Path dir) {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.writeAll((Path) null, list()));
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.writeAll(dir.resolve("x"),
                            (CustomList<Automobile>) null));
        }
    }

    // =====================================================================
    // writeAll(Writer, CustomList)
    // =====================================================================
    @Nested
    class WriteAllWriter {

        @Test
        void writesHeaderAndRecords() throws IOException {
            assertEquals(HEADER + "\nA;1;2\nB;3;4\n",
                    writeToString(auto("A", 1, 2), auto("B", 3, 4)));
        }

        @Test
        void emptyListWritesHeaderOnly() throws IOException {
            assertEquals(HEADER + "\n", writeToString(list()));
        }

        @Test
        void emitsHeaderOnEveryCall() throws IOException {
            StringWriter sw = new StringWriter();
            AutomobileFileWriter.writeAll(sw, list(auto("A", 1, 2)));
            AutomobileFileWriter.writeAll(sw, list(auto("B", 3, 4)));
            assertEquals(HEADER + "\nA;1;2\n" + HEADER + "\nB;3;4\n", sw.toString());
        }

        @Test
        void doesNotFlushOrCloseWriter() throws IOException {
            TrackingWriter w = new TrackingWriter();
            AutomobileFileWriter.writeAll(w, list(auto("A", 1, 2)));
            assertFalse(w.flushed, "writeAll must not flush caller-owned writer");
            assertFalse(w.closed,  "writeAll must not close caller-owned writer");
            assertEquals(HEADER + "\nA;1;2\n", w.toString());
        }

        @Test
        void validatesBeforeWriting() {
            TrackingWriter w = new TrackingWriter();
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(w, list(auto("bad;model", 1, 2))));
            assertEquals("", w.toString(), "nothing should be written when validation fails");
        }

        @Test
        void rejectsNullArguments() {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.writeAll((Writer) null, list()));
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.writeAll(new StringWriter(),
                            (CustomList<Automobile>) null));
        }
    }

    // =====================================================================
    // write(Path, Automobile) / write(Writer, Automobile)
    // =====================================================================
    @Nested
    class WriteSingle {

        @Test
        void writesSingleAutomobileToPath(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("auto.csv");
            AutomobileFileWriter.write(file, auto("BMW", 100, 2020));
            assertEquals(List.of(HEADER, "BMW;100;2020"), readLines(file));
        }

        @Test
        void writesSingleAutomobileToWriter() throws IOException {
            StringWriter sw = new StringWriter();
            AutomobileFileWriter.write(sw, auto("BMW", 100, 2020));
            assertEquals(HEADER + "\nBMW;100;2020\n", sw.toString());
        }

        @Test
        void rejectsNullArguments(@TempDir Path dir) {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.write((Path) null, auto("A", 1, 2)));
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.write(dir.resolve("x"), (Automobile) null));
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.write((Writer) null, auto("A", 1, 2)));
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.write(new StringWriter(), (Automobile) null));
        }
    }

    // =====================================================================
    // appendAll(Path, CustomList)
    // =====================================================================
    @Nested
    class AppendAllPath {

        @Test
        void createsFileWithHeaderWhenMissing(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.appendAll(file, list(auto("A", 1, 2)));
            assertEquals(List.of(HEADER, "A;1;2"), readLines(file));
        }

        @Test
        void emitsHeaderWhenFileIsEmpty(@TempDir Path dir) throws IOException {
            Path file = Files.createFile(dir.resolve("autos.csv"));
            AutomobileFileWriter.appendAll(file, list(auto("A", 1, 2)));
            assertEquals(List.of(HEADER, "A;1;2"), readLines(file));
        }

        @Test
        void noDuplicateHeaderWhenFileNonEmpty(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            Files.writeString(file, HEADER + "\nA;1;2\n");
            AutomobileFileWriter.appendAll(file, list(auto("B", 3, 4)));
            assertEquals(List.of(HEADER, "A;1;2", "B;3;4"), readLines(file));
        }

        @Test
        void multipleAppendsPreserveOrder(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.appendAll(file, list(auto("A", 1, 1)));
            AutomobileFileWriter.appendAll(file, list(auto("B", 2, 2)));
            AutomobileFileWriter.appendAll(file, list(auto("C", 3, 3)));
            assertEquals(List.of(HEADER, "A;1;1", "B;2;2", "C;3;3"), readLines(file));
        }

        @Test
        void usesLfOnly(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.appendAll(file, list(auto("A", 1, 2)));
            String s = Files.readString(file, StandardCharsets.UTF_8);
            assertFalse(s.contains("\r"));
            assertTrue(s.endsWith("\n"));
        }

        @Test
        void validationFailureDoesNotTouchFile(@TempDir Path dir) throws IOException {
            Path missing = dir.resolve("missing.csv");
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.appendAll(missing, list(auto("bad;model", 1, 2))));
            assertFalse(Files.exists(missing), "missing file must not be created");

            Path existing = dir.resolve("existing.csv");
            String original = HEADER + "\nold;1;2\n";
            Files.writeString(existing, original);
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.appendAll(existing, list(auto("bad;model", 1, 2))));
            assertEquals(original, Files.readString(existing), "existing file must be untouched");
        }

        @Test
        void rejectsNullArguments(@TempDir Path dir) {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.appendAll((Path) null, list()));
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.appendAll(dir.resolve("x"),
                            (CustomList<Automobile>) null));
        }
    }

    // =====================================================================
    // append(Path, Automobile)
    // =====================================================================
    @Nested
    class AppendSingle {

        // "appends to existing file" dropped — same code path as AppendAllPath.noDuplicateHeaderWhenFileNonEmpty

        @Test
        void createsFileWithHeader(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("auto.csv");
            AutomobileFileWriter.append(file, auto("A", 1, 2));
            assertEquals(List.of(HEADER, "A;1;2"), readLines(file));
        }

        @Test
        void nullAutoThrowsNpe(@TempDir Path dir) {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.append(dir.resolve("x"), (Automobile) null));
        }
    }

    // =====================================================================
    // Model validation
    // =====================================================================
    @Nested
    class Validation {

        /** Automobile.Builder accepts any String incl. null/empty — the guard lives in the writer. */
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                " BMW", "BMW ", "#BMW", "BM;W", "BM\nW", "BM\rW"
        })
        void rejectsInvalidModel(String model) {
            assertRejects(auto(model, 1, 2));
        }

        @ParameterizedTest
        @ValueSource(strings = {"BM#W", "BMW X5"})
        void acceptsValidModelWithSpecialChars(String model) {
            assertDoesNotThrow(() -> writeToString(auto(model, 1, 2)));
        }

        @Test
        void nullAutomobileInListIsRejected(@TempDir Path dir) {
            CustomList<Automobile> bad = list();
            bad.add(null); // CustomList accepts null; writer must reject it

            assertRejects(bad);
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.appendAll(dir.resolve("x"), bad));
        }
    }

    // =====================================================================
    // Round-trip: writer output → reader → same Automobiles
    // =====================================================================
    @Nested
    class RoundTrip {

        /** writeAll + appendAll → readAll(Path). */
        @Test
        void fileRoundTrip(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.writeAll(file, list(auto("BMW X5",       340, 2022)));
            AutomobileFileWriter.appendAll(file, list(auto("Toyota Camry", 250, 2020)));

            CustomList<Automobile> back = AutomobileFileReader.readAll(file);

            assertEquals(2, back.size());
            assertAll(
                    () -> assertEquals("BMW X5",       back.get(0).getModel()),
                    () -> assertEquals(340,            back.get(0).getPower()),
                    () -> assertEquals(2022,           back.get(0).getYear()),
                    () -> assertEquals("Toyota Camry", back.get(1).getModel()),
                    () -> assertEquals(250,            back.get(1).getPower()),
                    () -> assertEquals(2020,           back.get(1).getYear())
            );
        }

        /**
         * Writer→String→Reader. Exercises the documented "duplicate header is fine"
         * contract of writeAll(Writer, …): the second header must be skipped.
         */
        @Test
        void writerStringRoundTrip_duplicateHeaderIsSkipped() throws IOException {
            StringWriter sw = new StringWriter();
            AutomobileFileWriter.writeAll(sw, list(
                    auto("BMW X5",      340, 2022),
                    auto("Citroën C4",  130, 2019)));
            AutomobileFileWriter.writeAll(sw, list(
                    auto("Toyota Camry", 250, 2020))); // second header — must be ignored

            CustomList<Automobile> back = AutomobileFileReader.parse(sw.toString());

            assertEquals(3, back.size());
            assertAll(
                    () -> assertEquals("BMW X5",       back.get(0).getModel()),
                    () -> assertEquals("Citroën C4",   back.get(1).getModel()),
                    () -> assertEquals("Toyota Camry", back.get(2).getModel())
            );
        }

        /**
         * Model with {@code '#'} not at start: writer accepts it, reader sees the line as
         * a record (not a comment). Verifies the two rules agree.
         */
        @Test
        void modelWithInternalHashRoundTrips() throws IOException {
            CustomList<Automobile> back =
                    AutomobileFileReader.parse(writeToString(auto("BM#W", 100, 2020)));

            assertEquals(1, back.size());
            assertEquals("BM#W", back.get(0).getModel());
        }
    }
}
