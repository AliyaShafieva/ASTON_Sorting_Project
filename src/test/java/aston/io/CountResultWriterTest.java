package aston.io;

import aston.core.Automobile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountResultWriterTest {

    @Test
    void appendsLineEachTime(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("result.txt");
        Automobile car = new Automobile.Builder().model("Kia").power(120).year(2020).build();

        CountResultWriter.append(file, car, 3);
        CountResultWriter.append(file, car, 5);

        List<String> lines = Files.readAllLines(file);
        assertEquals(2, lines.size());
        assertEquals("Элемент " + car + " встречается в коллекции 3 раз(а)", lines.get(0));
        assertEquals("Элемент " + car + " встречается в коллекции 5 раз(а)", lines.get(1));
    }
}
