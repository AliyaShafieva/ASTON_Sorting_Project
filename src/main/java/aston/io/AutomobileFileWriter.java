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
 * Пишет автомобили в формате, который читает {@link AutomobileFileReader}
 * (UTF-8):
 *
 * <pre>
 * # model;power;year
 * BMW X5;340;2022
 * Toyota Camry;250;2020
 * </pre>
 *
 * <p>
 * writeAll / write начинают вывод с заголовка {@code #};
 * appendAll / append пишут заголовок только при создании нового или пустого файла.
 * Терминатор строки — {@code '\n'}, чтобы файлы были побайтово одинаковы на всех ОС.
 *
 * <p>
 * Каждый {@code model} проверяется заранее, чтобы пройти через
 * {@link AutomobileFileReader} без искажений: не {@code null}, не пустой,
 * без пробелов по краям, без {@code '#'} первым символом, без {@code ';'},
 * {@code '\n'}, {@code '\r'}.
 *
 * <p>
 * {@link #writeAll(Path, CustomList)} проверяет данные <b>до</b> любого ввода-вывода
 * и пишет во временный файл. Если перемещение выполняется не атомарно и возникает
 * IOException, состояние файлов не определено. У {@link #writeAll(Writer, CustomList)}
 * этой гарантии нет (поток принадлежит вызывающему), и заголовок {@code #} пишется
 * на <b>каждый</b> вызов — поэтому метод не композируется для дописывания; reader
 * всё равно разбирает такой вывод корректно (пропускает строки {@code #}).
 *
 * <p>
 * Замечание: строка с <b>пустым</b> model ({@code ;340;2022}), вручную
 * добавленная в файл, сейчас принимается reader'ом, но отвергается writer'ом —
 * асимметрия исчезнет, когда в {@link AutomobileFileReader} появится
 * {@code TODO(validation)} с симметричной проверкой пустого model.
 *
 * <p>
 * Требует, чтобы {@code AutomobileFileReader.DELIMITER} был package-private.
 */

public final class AutomobileFileWriter {

    private static final String HEADER = "# model;power;year";

    private AutomobileFileWriter() {
        // утилитный класс
    }

    /**
     * Пишет все автомобили в файл (UTF-8). Создаёт файл или перезаписывает
     * существующий. Перезапись идёт через временный файл в каталоге цели и
     * атомарное перемещение, если ФС это поддерживает; иначе — best-effort
     * неатомарная замена (см. замечание о неатомарных перемещениях).
     * <p>
     * <b>Оговорка POSIX:</b> перезапись заменяет файл временным, поэтому на POSIX
     * права цели становятся правами временного файла (обычно {@code 0600}).
     * Если нужны group/other-права, вызывающий должен восстановить их после записи.
     * На не-POSIX ФС (например Windows) это неактуально — ACL наследуются от
     * родительского каталога.
     */
    public static void writeAll(Path path, CustomList<Automobile> autos) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(autos, "autos");
        validateAll(autos); // ДО любого файлового I/O

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
                // ФС не умеет атомарную замену — best-effort fallback
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Пишет в переданный writer.
     * <p>
     * <b>Writer принадлежит вызывающему: метод его НЕ flush'ит и НЕ закрывает.</b>
     * Заголовок {@code #} пишется на каждый вызов (не композируется для дописывания;
     * reader дубликат заголовка переживает).
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
     * Пишет один автомобиль в файл (UTF-8), эквивалент
     * {@code writeAll(path, [auto])}. Создаёт или атомарно перезаписывает файл.
     */

    public static void write(Path path, Automobile auto) throws IOException {
        Objects.requireNonNull(auto, "auto");
        writeAll(path, singleton(auto));
    }

    /**
     * Пишет один автомобиль в переданный writer, эквивалент
     * {@code writeAll(writer, [auto])}.
     * <p>
     * <b>Writer принадлежит вызывающему: метод его НЕ flush'ит и НЕ закрывает.</b>
     */
    public static void write(Writer writer, Automobile auto) throws IOException {
        Objects.requireNonNull(auto, "auto");
        writeAll(writer, singleton(auto));
    }

    /**
     * Пишет записи в переданный writer. Решение о заголовке — за вызывающим.
     */
    private static void writeRecords(Writer writer, CustomList<Automobile> autos) throws IOException {
        for (Automobile a : autos) {
            writer.write(format(a));
            writer.write('\n');
        }
    }

    /**
     * Дописывает автомобили в файл (UTF-8). Создаёт файл (с заголовком), если он
     * отсутствует или пуст; иначе дописывает только записи, без дубликата заголовка.
     * <p>
     * <b>Не атомарно:</b> если запись сорвётся посередине, файл может закончиться
     * неполной строкой. Reader сообщит об этой строке при следующем чтении.
     * <p>
     * <b>Права и содержимое существующего файла — на вызывающем</b>; метод не
     * усекает файл.
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
     * Дописывает один автомобиль — эквивалент {@code appendAll(path, [auto])}.
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
                || !model.equals(model.trim()) // trim(), НЕ strip(): должен совпадать с reader
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
