package aston.sort;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/// Запускает ручные тесты сортировки
public class SortTestsTest {
    @Test
    void sortingAlgorithmsPassAllChecks() {
        SortStrategyTest.main(new String[0]);
        assertEquals(0, SortTestHelper.failedCount(), "есть упавшие проверки - смотри вывод выше");
        assertEquals(22, SortTestHelper.passedCount(), "выполнились не все проверки");
    }

    @Test
    void evenValuesSorterPassesAllChecks() {
        EvenValuesSorterTest.main(new String[0]);
        assertEquals(0, SortTestHelper.failedCount(), "есть упавшие проверки - смотри вывод выше");
        assertEquals(14, SortTestHelper.passedCount(), "выполнились не все проверки");
    }
}
