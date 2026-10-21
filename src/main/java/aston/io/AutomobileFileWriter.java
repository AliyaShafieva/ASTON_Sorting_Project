package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Writes automobiles in the format read by {@link AutomobileFileReader}
 * (UTF-8):
 * 
 * <pre>
 * # model;power;year
 * BMW X5;340;2022
 * Toyota Camry;250;2020
 * </pre>
 *
 * <p>
 * writeAll / write output begins with the # header line;
 * appendAll / append emit the header only when creating a new or empty file.
 * Uses {@code '\n'} as the line terminator so files are byte-identical
 * across OSes.
 *
 * <p>
 * Every {@code model} is validated up-front to ensure it round-trips through
 * {@link AutomobileFileReader} unchanged: it must be non-null, non-empty,
 * un-padded,
 * and free of {@code '#'} as first char, {@code ';'}, {@code '\n'},
 * {@code '\r'}.
 *
 * <p>
 * The {@link #writeAll(Path, CustomList)} overload validates <b>before</b> any
 * file I/O and writes to a temp file.
 * When the move is performed as a non-atomic operation, and an IOException is
 * thrown, then the state of the files is not defined. The
 * {@link #writeAll(Writer, CustomList)}
 * overload does not provide this property (the caller owns the stream), and
 * emits
 * the {@code #} header on <b>every</b> call, so it is not self-composable for
 * appending; the reader still parses such output correctly (it skips
 * {@code #}-lines).
 *
 * <p>
 * Note: a hand-edited file line with an <b>empty</b> model ({@code ;340;2022})
 * is currently accepted by the reader but rejected by this writer — this
 * asymmetry
 * will be resolved when {@link AutomobileFileReader}'s {@code TODO(validation)}
 * adds the symmetric empty-model check.
 *
 * <p>
 * Requires {@code AutomobileFileReader.DELIMITER} to be package-private.
 */
public final class AutomobileFileWriter {

    private static final String HEADER = "# model;power;year";

    private AutomobileFileWriter() {
        // utility class
    }

    /**
     * Writes all automobiles to a file (UTF-8). Creates the file, or overwrites
     * an existing one. Overwriting uses a temp file in the target's directory
     * followed by an atomic move where the filesystem supports it; if atomic
     * replacement is not supported, a best-effort non-atomic replace is used
     * instead (see the class-level note on non-atomic moves).
     * <p>
     * <b>POSIX caveat:</b> overwriting replaces the file with the temp file, so
     * on POSIX filesystems the target's permissions become the temp file's
     * (typically {@code 0600}). Callers who need to preserve group/other access
     * must reapply permissions after the write. On non-POSIX filesystems (e.g.
     * Windows) this caveat does not apply — ACLs are inherited from the parent
     * directory.
     */
    public static void writeAll(Path path, CustomList<Automobile> autos) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(autos, "autos");
        validateAll(autos); // BEFORE any file I/O

        Path target = path.toAbsolutePath();
        Path dir = target.getParent();
        Path temp = Files.createTempFile(dir, "autos-", ".tmp");

        try {
            try (Writer w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                w.write(HEADER);
                w.write('\n');
                writeRecords(w, autos);
            }
            try {
                Files.move(temp, target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException | FileAlreadyExistsException e) {
                // Filesystem can't do atomic replace — fall back to best-effort.
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Writes to a caller-provided writer.
     * <p>
     * <b>The caller owns {@code writer}: this method neither flushes nor closes
     * it.</b>
     * Emits the {@code #} header on every call (not self-composable for append; the
     * reader handles the duplicate header fine).
     */
    public static void writeAll(Writer writer, CustomList<Automobile> autos) throws IOException {
        Objects.requireNonNull(writer, "writer");
        Objects.requireNonNull(autos, "autos");
        validateAll(autos);
        writer.write(HEADER);
        writer.write('\n');
        writeRecords(writer, autos);
    }

    /**
     * Writes a single automobile to a file (UTF-8), equivalent to
     * {@code writeAll(path, [auto])}. Creates or overwrites the file atomically.
     */
    public static void write(Path path, Automobile auto) throws IOException {
        Objects.requireNonNull(auto, "auto");
        writeAll(path, singleton(auto));
    }

    /**
     * Writes a single automobile to a caller-provided writer, equivalent to
     * {@code writeAll(writer, [auto])}.
     * <p>
     * <b>The caller owns {@code writer}: this method neither flushes nor closes
     * it.</b>
     */
    public static void write(Writer writer, Automobile auto) throws IOException {
        Objects.requireNonNull(auto, "auto");
        writeAll(writer, singleton(auto));
    }

    /**
     * Writes records to a caller-provided writer. Caller decides whether to emit
     * the header.
     */
    private static void writeRecords(Writer writer, CustomList<Automobile> autos) throws IOException {
        for (Automobile a : autos) {
            writer.write(format(a));
            writer.write('\n');
        }
    }

    /**
     * Appends automobiles to a file (UTF-8). Creates the file (with header) if
     * missing or empty; otherwise appends records only, without a duplicate header.
     * <p>
     * <b>Not atomic:</b> if the write fails mid-way, the file may end with a
     * partial record. The reader will report that line on its next read.
     * <p>
     * <b>Caller owns the file's existing permissions/contents</b>; this method does
     * not truncate.
     */
    public static void appendAll(Path path, CustomList<Automobile> autos) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(autos, "autos");
        validateAll(autos);

        boolean needsHeader = !Files.exists(path) || Files.size(path) == 0;
        try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (needsHeader) {
                w.write(HEADER);
                w.write('\n');
            }
            writeRecords(w, autos);
        }
    }

    /**
     * Appends a single automobile — equivalent to {@code appendAll(path, [auto])}.
     */
    public static void append(Path path, Automobile auto) throws IOException {
        Objects.requireNonNull(auto, "auto");
        appendAll(path, singleton(auto));
    }

    private static CustomList<Automobile> singleton(Automobile auto) {
        CustomList<Automobile> list = new CustomList<>();
        list.add(auto);
        return list;
    }

    private static void validateAll(CustomList<Automobile> autos) {
        for (Automobile a : autos) {
            if (a == null) {
                throw new IllegalArgumentException("automobile is null");
            }
            validateModel(a.getModel());
        }
    }

    private static void validateModel(String model) {
        if (model == null
                || model.isEmpty()
                || !model.equals(model.trim()) // keep trim(), NOT strip(): must match reader
                || model.startsWith("#")
                || model.indexOf(';') >= 0
                || model.indexOf('\n') >= 0
                || model.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(
                    "model cannot be represented in the file format: " + quote(model));
        }
    }

    private static String format(Automobile a) {
        return a.getModel()
                + AutomobileFileReader.DELIMITER
                + a.getPower()
                + AutomobileFileReader.DELIMITER
                + a.getYear();
    }

    private static String quote(String s) {
        return s == null ? "null" : "\"" + s + "\"";
    }
}
