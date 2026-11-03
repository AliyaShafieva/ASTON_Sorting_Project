package aston.stream;

import aston.core.Automobile;
import aston.util.CustomList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StreamDataFillerTest {

    @Test
    void testFillCustomListGeneratesValidData() {
        int count = 20;
        CustomList<Automobile> list = StreamDataFiller.fillCustomList(count);

        assertEquals(count, list.size());

        for (Automobile auto : list) {
            assertAll("Проверка полей автомобиля",
                    () -> assertNotNull(auto.getModel(), "Модель не должна быть null"),
                    () -> assertTrue(auto.getPower() >= 80 && auto.getPower() <= 430,
                            "Мощность вне диапазона: " + auto.getPower()),
                    () -> assertTrue(auto.getYear() >= 2000 && auto.getYear() <= 2026,
                            "Год вне диапазона: " + auto.getYear())
            );
        }
    }

    @Test
    void testEmptyGeneration() {
        CustomList<Automobile> list = StreamDataFiller.fillCustomList(0);
        assertTrue(list.isEmpty(), "Список должен быть пустым при count=0");
    }
}
