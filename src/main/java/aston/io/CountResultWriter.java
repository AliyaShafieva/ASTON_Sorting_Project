package aston.io;

import aston.core.Automobile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class CountResultWriter {

    private CountResultWriter() {} // утилит

    public static void append(Path path, Automobile target, long count) throws IOException {
        String line = "Элемент " + target + " встречается в коллекции " + count + " раз(а)"
                + System.lineSeparator();
        Files.writeString(path, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
