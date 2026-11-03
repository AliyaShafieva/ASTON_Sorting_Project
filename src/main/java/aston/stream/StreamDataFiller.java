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
    private static final String[] MODELS = {
            "BMW X5", "Toyota Camry", "Audi A4", "Mercedes C-Class",
            "Tesla Model 3", "Ford Mustang", "Honda Civic", "Mazda 6",
            "Skoda Octavia", "Volkswagen Golf", "Kia Sportage", "Hyundai Solaris",
            "Lada Vesta", "Lada Granta", "Lada Niva", "UAZ Patriot",
            "UAZ Hunter", "Moskvich 3", "GAZ Volga", "Lada Largus"
    };
    public static CustomList<Automobile> fillCustomList(int count) {
        CustomList<Automobile> customList = new CustomList<>();
        Random random = new Random();
        // Stream.generate создает бесконечный поток объектов через Builder
        // limit(count) обрезает поток до нужного количества
        // forEach(auto -> customList.add(auto)) — сохраняет каждый сгенерированный объект в CustomList
        Stream.generate(() -> new Automobile.Builder()
                        .model(MODELS[random.nextInt(MODELS.length)])
                        .power(80 + random.nextInt(350))
                        .year(2000 + random.nextInt(26))
                        .build())
                .limit(count)
                .forEach(auto -> customList.add(auto));

        return customList;
    }

}
