package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;
import java.io.IOException;
import java.nio.file.*;
import java.util.Random;
import java.util.Scanner;

public class FillArray {

    private static final Path filePath = Paths.get("src/main/resources/automobiles-input.csv");

    public static CustomList fiilFromFile(CustomList customList) throws IOException {
        CustomList<Automobile> automobiles = AutomobileFileReader.readAll(filePath);

        for (int i = 0; i < automobiles.size(); i++) {
            customList.add(automobiles.get(i));
        }

        return customList;
    }

    /// созданный метод для заполнение списка вручную
    public static CustomList fillHandle(CustomList customList, int lenghtArray) {
        return customList;
    }

    /// созданный метод для заполнение списка рандомными значениями
    public static CustomList fillRandom(CustomList customList, int lenghtArray) {

        return customList;
    }
}