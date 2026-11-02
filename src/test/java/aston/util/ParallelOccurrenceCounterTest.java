package aston.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParallelOccurrenceCounterTest {

    // Вспомогательный метод для создания списка
    private CustomList<Integer> createList(Integer... values) {
        CustomList<Integer> list = new CustomList<>();

        for (Integer value : values) {
            list.add(value);
        }

        return list;
    }

    @Test
    //несколько повторений элемента
    void countsMultipleOccurrences() {
        CustomList<Integer> list =
                createList(10, 20, 10, 30, 10, 40, 20, 10);

        long result = ParallelOccurrenceCounter.countOccurrences(
                list, 10, 4
        );

        assertEquals(4, result);
    }

    @Test
    //элемента нет в коллекции
    void returnsZeroWhenElementIsAbsent() {
        CustomList<Integer> list = createList(1, 2, 3, 4, 5);

        long result = ParallelOccurrenceCounter.countOccurrences(
                list, 10, 3
        );

        assertEquals(0, result);
    }

    @Test
    //коллекция из одного элемента
    void worksWithSingleElement() {
        CustomList<Integer> list = createList(7);

        long result = ParallelOccurrenceCounter.countOccurrences(
                list, 7, 4
        );

        assertEquals(1, result);
    }

    @Test
    //пустая коллекция
    void worksWithEmptyCollection() {
        CustomList<Integer> list = createList();

        long result = ParallelOccurrenceCounter.countOccurrences(
                list, 10, 4
        );

        assertEquals(0, result);
    }

    @Test
    //потоков больше, чем элементов
    void worksWhenThreadCountExceedsCollectionSize() {
        CustomList<Integer> list = createList(5, 5, 5);

        long result = ParallelOccurrenceCounter.countOccurrences(
                list, 5, 10
        );

        assertEquals(3, result);
    }

    @Test
    //поиск null
    void worksWithNullElements() {
        CustomList<Integer> list =
                createList(1, null, 2, null, 3);

        long result = ParallelOccurrenceCounter.countOccurrences(
                list, null, 2
        );

        assertEquals(2, result);
    }

    @Test
    //передали null вместо коллекции
    void throwsExceptionWhenCollectionIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> ParallelOccurrenceCounter.countOccurrences(
                        null, 10, 2
                )
        );
    }

    @Test
    //ноль потоков
    void throwsExceptionWhenThreadCountIsZero() {
        CustomList<Integer> list = createList(1, 2, 3);

        assertThrows(
                IllegalArgumentException.class,
                () -> ParallelOccurrenceCounter.countOccurrences(
                        list, 2, 0
                )
        );
    }

    @Test
    //отрицательное число потоков
    void throwsExceptionWhenThreadCountIsNegative() {
        CustomList<Integer> list = createList(1, 2, 3);

        assertThrows(
                IllegalArgumentException.class,
                () -> ParallelOccurrenceCounter.countOccurrences(
                        list, 2, -1
                )
        );
    }
}
