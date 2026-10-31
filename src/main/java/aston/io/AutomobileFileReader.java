package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Читает и парсит автомобили в UTF-8 формате, совместимом с
 * {@link AutomobileFileWriter}.
 *
 * <p>
 * Формат: одна запись на строку, поля через {@code ;}.
 * Строки с {@code #} в начале и пустые строки игнорируются.
 * Заголовок не обязателен — назначение колонок задаёт строка {@code #}:
 *
 * <pre>
 * # model;power;year
 * BMW X5;340;2022
 * Toyota Camry;250;2020
 * </pre>
 *
 * <p>
 * Валидация значений (число полей, непустая модель, power &gt; 0,
 * правдоподобный год) <b>пока не реализована</b> — см.
 * {@code TODO(validation)}.
 */
public final class AutomobileFileReader {

    static final String DELIMITER = ";";

    private AutomobileFileReader() {
        // утилитный класс
    }

    /**
     * Читает все автомобили из файла (UTF-8). Открывает и закрывает файл.
     */
    public static CustomList<Automobile> readAll(Path path) throws IOException {
        Objects.requireNonNull(path, "path");

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return readAll(br); // br закрывается через try-with-resources
        }
    }

    /**
     * Читает из переданного reader'а.
     * <p>
     * <b>Reader принадлежит вызывающему: метод его НЕ закрывает.</b>
     */
    public static CustomList<Automobile> readAll(Reader reader) throws IOException {
        Objects.requireNonNull(reader, "reader");

        BufferedReader br = (reader instanceof BufferedReader)
                ? (BufferedReader) reader
                : new BufferedReader(reader);

        CustomList<Automobile> result = new CustomList<>();
        String line;
        int lineNo = 0;

        while ((line = br.readLine()) != null) {
            lineNo++;

            if (lineNo == 1 && line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            result.add(parseLine(line, lineNo));
        }
        return result;
    }

    /**
     * Парсит автомобили из CSV-содержимого. Файловый ввод-вывод не выполняет,
     * ничего не закрывает; на вход идёт уже прочитанное содержимое файла.
     *
     * @param csvContent CSV-текст, <b>не</b> путь к файлу
     * @throws IllegalArgumentException если запись не удаётся разобрать
     * @throws NullPointerException     если {@code csvContent} равен {@code null}
     */
    public static CustomList<Automobile> parse(String csvContent) {
        Objects.requireNonNull(csvContent, "csvContent");
        try {
            return readAll(new StringReader(csvContent));
        } catch (IOException e) {
            // StringReader не бросает IOException; защитная обёртка.
            throw new UncheckedIOException("unexpected I/O error parsing string", e);
        }
    }

    private static Automobile parseLine(String line, int lineNo) {

        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Line " + lineNo + ": line is empty or null");
        }

        String[] parts = line.split(DELIMITER, -1);

        if (parts.length < 3) {
            throw new IllegalArgumentException("Line " + lineNo + ": insufficient data fields in \"" + line + "\"");
        }

        String model = parts[0].trim();

        if (model.isEmpty()) {
            throw new IllegalArgumentException("Line " + lineNo + ": car model cannot be blank");
        }

        int power, year;

        try {
            power = Integer.parseInt(parts[1].trim());
            year  = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Line " + lineNo + ": bad number in \"" + line + "\"", e);
        }

        if (power <= 0) {
            throw new IllegalArgumentException("Line " + lineNo + ": engine power must be greater than 0, got: " + power);
        }

        int currentYear = java.time.Year.now().getValue();

        if (year < 1900 || year > currentYear + 1) {
            throw new IllegalArgumentException(
                    "Line " + lineNo + ": year must be between 1900 and " + (currentYear + 1) + ", got: " + year);
        }

        return new Automobile.Builder()
                .model(model)
                .power(power)
                .year(year)
                .build();
    }
}
