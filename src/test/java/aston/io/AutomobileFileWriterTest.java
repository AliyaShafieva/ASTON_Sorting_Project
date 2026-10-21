package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
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
 * <p>Round-trip tests use {@link AutomobileFileReader#readAll(String)} and
 * {@link AutomobileFileReader#readAll(Path)} to assert the writer→reader contract
 * end-to-end, not just the byte format.
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

        @Test
        void utf8EncodedBytesAndLfOnly(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.writeAll(file, list(auto("Citroën", 120, 2019)));
            byte[] expected = (HEADER + "\nCitroën;120;2019\n").getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expected, Files.readAllBytes(file));
        }

        @Test
        void validatesBeforeIo_noFileCreated(@TempDir Path dir) {
            Path file = dir.resolve("autos.csv");
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(file, list(auto("bad;model", 1, 2))));
            assertFalse(Files.exists(file), "file must not be created when validation fails");
        }

        @Test
        void leavesOriginalIntactOnValidationFailure(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            Files.writeString(file, "original\n");
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(file, list(auto("bad;model", 1, 2))));
            assertEquals("original\n", Files.readString(file));
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
        void nullPathThrowsNpe() {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.writeAll((Path) null, list()));
        }

        @Test
        void nullListThrowsNpe(@TempDir Path dir) {
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
            StringWriter sw = new StringWriter();
            AutomobileFileWriter.writeAll(sw, list(auto("A", 1, 2), auto("B", 3, 4)));
            assertEquals(HEADER + "\nA;1;2\nB;3;4\n", sw.toString());
        }

        @Test
        void emptyListWritesHeaderOnly() throws IOException {
            StringWriter sw = new StringWriter();
            AutomobileFileWriter.writeAll(sw, list());
            assertEquals(HEADER + "\n", sw.toString());
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
        void nullWriterThrowsNpe() {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.writeAll((Writer) null, list()));
        }

        @Test
        void nullListThrowsNpe() {
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
        void nullAutoPathThrowsNpe(@TempDir Path dir) {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.write(dir.resolve("x"), (Automobile) null));
        }

        @Test
        void nullAutoWriterThrowsNpe() {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.write(new StringWriter(), (Automobile) null));
        }

        @Test
        void nullWriterThrowsNpe() {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.write((Writer) null, auto("A", 1, 2)));
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
        void validatesBeforeIo(@TempDir Path dir) {
            Path file = dir.resolve("autos.csv");
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.appendAll(file, list(auto("bad;model", 1, 2))));
            assertFalse(Files.exists(file));
        }

        @Test
        void leavesExistingFileIntactOnValidationFailure(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            String original = HEADER + "\nold;1;2\n";
            Files.writeString(file, original);
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.appendAll(file, list(auto("bad;model", 1, 2))));
            assertEquals(original, Files.readString(file));
        }

        @Test
        void nullPathThrowsNpe() {
            assertThrows(NullPointerException.class,
                    () -> AutomobileFileWriter.appendAll((Path) null, list()));
        }

        @Test
        void nullListThrowsNpe(@TempDir Path dir) {
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

        @Test
        void createsFileWithHeader(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("auto.csv");
            AutomobileFileWriter.append(file, auto("A", 1, 2));
            assertEquals(List.of(HEADER, "A;1;2"), readLines(file));
        }

        @Test
        void appendsWithoutHeaderWhenFileExists(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("auto.csv");
            Files.writeString(file, HEADER + "\nA;1;2\n");
            AutomobileFileWriter.append(file, auto("B", 3, 4));
            assertEquals(List.of(HEADER, "A;1;2", "B;3;4"), readLines(file));
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
        @ValueSource(strings = {
                "", " BMW", "BMW ", "#BMW", "BM;W", "BM\nW", "BM\rW"
        })
        void rejectsInvalidModel(String model) {
            Automobile a = auto(model, 1, 2);
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(new StringWriter(), list(a)));
        }

        @Test
        void rejectsNullModel() {
            Automobile a = auto(null, 1, 2); // Builder stores it; writer must reject
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(new StringWriter(), list(a)));
        }

        @Test
        void acceptsHashNotAtStart() {
            assertDoesNotThrow(() ->
                    AutomobileFileWriter.writeAll(new StringWriter(), list(auto("BM#W", 1, 2))));
        }

        @Test
        void acceptsInternalSpace() {
            assertDoesNotThrow(() ->
                    AutomobileFileWriter.writeAll(new StringWriter(), list(auto("BMW X5", 1, 2))));
        }

        @Test
        void nullAutomobileInListIsRejected() {
            CustomList<Automobile> l = new CustomList<>();
            l.add(null); // CustomList accepts null; writer must reject it
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.writeAll(new StringWriter(), l));
        }

        @Test
        void nullAutomobileInAppendListIsRejected(@TempDir Path dir) {
            CustomList<Automobile> l = new CustomList<>();
            l.add(null);
            assertThrows(IllegalArgumentException.class,
                    () -> AutomobileFileWriter.appendAll(dir.resolve("x"), l));
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

            CustomList<Automobile> back = AutomobileFileReader.readAll(sw.toString());

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
            StringWriter sw = new StringWriter();
            AutomobileFileWriter.writeAll(sw, list(auto("BM#W", 100, 2020)));

            CustomList<Automobile> back = AutomobileFileReader.readAll(sw.toString());

            assertEquals(1, back.size());
            assertEquals("BM#W", back.get(0).getModel());
        }

        /** Pins the exact bytes emitted by writeAll(Path, …), independent of reader leniency. */
        @Test
        void writerOutputBytes_areExactlyHeaderPlusLfRecords(@TempDir Path dir) throws IOException {
            Path file = dir.resolve("autos.csv");
            AutomobileFileWriter.writeAll(file, list(
                    auto("BMW X5",       340, 2022),
                    auto("Toyota Camry", 250, 2020)));

            byte[] expected = (
                    HEADER + "\n" +
                    "BMW X5;340;2022\n" +
                    "Toyota Camry;250;2020\n"
            ).getBytes(StandardCharsets.UTF_8);

            assertArrayEquals(expected, Files.readAllBytes(file));
            assertEquals(1, Files.readAllLines(file).stream()
                    .filter(HEADER::equals).count(),
                    "exactly one header line expected");
        }
    }
}
