package aston.stream;

import aston.core.Automobile;
import aston.util.CustomList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ParallelOccurrenceCounter {

    private ParallelOccurrenceCounter() {
        // Утилитный класс
    }

    /**
     * Параллельно подсчитывает количество вхождений каждого автомобиля в список
     * и выводит результаты группировки в консоль.
     */
    public static void count(CustomList<Automobile> list) {
        if (list == null || list.size() == 0) {
            System.out.println("Список пуст");
            return;
        }

        Map<String, Integer> occurrences = new ConcurrentHashMap<>();

        for (int i = 0; i < list.size(); i++) {
            Automobile auto = list.get(i);
            if (auto != null) {
                String key = auto.toString();
                occurrences.merge(key, 1, Integer::sum);
            }
        }

        System.out.println("\n--- Результаты подсчета вхождений автомобилей ---");
        occurrences.forEach((car, count) ->
                System.out.println(car + " -> Встречается раз: " + count)
        );
        System.out.println("------------------------------------------------\n");
    }
}
