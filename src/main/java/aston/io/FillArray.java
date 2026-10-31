package aston.io;

import aston.core.Automobile;
import aston.util.CustomList;
import java.io.IOException;
import java.nio.file.*;

public class FillArray {

    private static final Path filePath = Paths.get("src/main/resources/automobiles-input.csv");

    public static CustomList<Automobile> fillFromFile(CustomList<Automobile> customList) throws IOException {
        CustomList<Automobile> automobiles = AutomobileFileReader.readAll(filePath);

        for (int i = 0; i < automobiles.size(); i++) {
            Automobile auto = automobiles.get(i);

            if (auto != null) {
                customList.add(auto);
            }
        }

        return customList;
    }
}
