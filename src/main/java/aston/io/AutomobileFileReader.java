package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Reads automobiles from a text file (UTF-8).
 *
 * <p>Format: one record per line, fields separated by {@code ;}.
 * Lines starting with {@code #} and blank lines are ignored.
 * No header row is required — column meaning is documented by a leading {@code #} line:
 * <pre>
 * # model;power;year
 * BMW X5;340;2022
 * Toyota Camry;250;2020
 * </pre>
 *
 * <p>Value validation (field count, non-empty model, power &gt; 0, plausible year)
 * is <b>intentionally not implemented yet</b> — see {@code TODO(validation)} below.
 */
public final class AutomobileFileReader {

    static final String DELIMITER = ";";
    private static final Pattern SPLIT_PATTERN = Pattern.compile(Pattern.quote(DELIMITER));

    private AutomobileFileReader() {
        // utility class
    }

    /** Reads all automobiles from a file (UTF-8). This method opens and closes the file. */
    public static CustomList<Automobile> readAll(Path path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return readAll(br); // br owned here → closed by try-with-resources
        }
    }

    /**
     * Reads from a caller-provided reader.
     * <p><b>The caller owns {@code reader}: this method does NOT close it.</b>
     */
    public static CustomList<Automobile> readAll(Reader reader) throws IOException {
        BufferedReader br = (reader instanceof BufferedReader)
                ? (BufferedReader) reader
                : new BufferedReader(reader);

        CustomList<Automobile> result = new CustomList<>();
        String line;
        int lineNo = 0;

        while ((line = br.readLine()) != null) {
            lineNo++;

            // Strip BOM (U+FEFF)
            if (lineNo == 1 && line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue; // comment or blank line
            }

            result.add(parseLine(line, lineNo));
        }
        return result;
    }

    /** Convenience overload for tests: reads from a string. */
    public static CustomList<Automobile> readAll(String csvContent) throws IOException {
        return readAll(new StringReader(csvContent));
    }

    private static Automobile parseLine(String line, int lineNo) {
        String[] parts = line.split(SPLIT_PATTERN.pattern(), -1); // -1 keeps trailing empty fields

        // TODO(validation): ensure parts.length == FIELD_COUNT (3) 
        // and report lineNo + original line

        String model = parts[0].trim();

        int power;
        int year;
        try {
            power = Integer.parseInt(parts[1].trim());
            year  = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            // Not validation — just line context for the raw parse error.
            throw new IllegalArgumentException(
                    "Line " + lineNo + ": bad number in \"" + line + "\"", e);
        }

        // TODO(validation): model must not be blank
        // TODO(validation): power must be > 0
        // TODO(validation): year must be in plausible range (e.g. 1900..currentYear+1)

        return new Automobile.Builder()
                .model(model)
                .power(power)
                .year(year)
                .build();
    }
}
