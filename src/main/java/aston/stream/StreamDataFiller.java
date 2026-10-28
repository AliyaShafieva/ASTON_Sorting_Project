package aston.stream;

import aston.core.Automobile;
import aston.util.CustomList;
import java.util.stream.Stream;
import java.util.Random;

/**
 * Класс-генератор для наполнения коллекции CustomList.
 * Реализует логику заполнения данных через Stream API.
 */
public class StreamDataFiller {

    /**
     * Генерирует случайные объекты Automobile и добавляет их в CustomList.
     * Использует Stream.generate для создания потока данных и limit для ограничения размера.
     * count количество машин, которые нужно сгенерировать
     */
    public static CustomList<Automobile> fillCustomList(int count) {
        CustomList<Automobile> customList = new CustomList<>();
        Random random = new Random();

        // Stream.generate создает бесконечный поток объектов через Builder
        // limit(count) обрезает поток до нужного количества
        // forEach(auto -> customList.add(auto)) — сохраняет каждый сгенерированный объект в CustomList
        Stream.generate(() -> new Automobile.Builder()
                        .model("Model-" + (100 + random.nextInt(900)))
                        .power(100 + random.nextInt(300))
                        .year(2000 + random.nextInt(26))
                        .build())
                .limit(count)
                .forEach(auto -> customList.add(auto));
        return customList;
    }
}
