package aston.sort;

import aston.core.Automobile;
import aston.util.CustomList;
import java.util.function.ToIntFunction;
/// Общие вспомогатеельные методы для тестов: проверка результата, подсчёт итогов
/// и короткие способы создать тестовые данные.
public class SortTestHelper {
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private static int passed = 0;
    private static int failed = 0;

    private SortTestHelper() {
    }
    /// Обнуляет счётчики - вызывается в начале каждого тестового класса.
    static void reset() {
        passed = 0;
        failed = 0;
    }
    /// Сравнивает результат с ожидаемым и печатает исходные данные, ожидание и результат.
    ///
    /// @param name     название проверки
    /// @param before   данные до сортировки
    /// @param actual   что получилось после сортировки
    /// @param expected что должно было получиться
    static void check(String name, String before, String actual, String expected) {
        boolean ok = actual.equals(expected);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        System.out.println((ok ? GREEN + "OK    " : RED + "FAIL  ") + RESET + name);
        System.out.println("        было:     [" + before + "]");
        System.out.println("        ожидали:  [" + expected + "]");
        System.out.println("        получили: [" + actual + "]" + (ok ? "" : "   <-- не совпадает"));
    }
    /// Печатает итог: сколько проверок прошло и сколько упало.
    static void printTotal() {
        System.out.println("Итого: " + GREEN + "прошло " + passed + RESET + ", " + RED + "упало " + failed);
    }

    static Automobile car(String model, int power, int year) {
        return new Automobile.Builder().model(model).power(power).year(year).build();
    }

    static CustomList<Integer> numbers(int... values) {
        CustomList<Integer> list = new CustomList<>();
        for (int v : values) {
            list.add(v);
        }
        return list;
    }
    /// Модели машин через пробел, например `Audi BMW Kia`.
    static String models(CustomList<Automobile> cars) {
        StringBuilder sb = new StringBuilder();
        for (Automobile car : cars) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(car.getModel());
        }
        return sb.toString();
    }
    /// Модели вместе со значением поля, например `Lada(90) Kia(120)`.
    static String models(CustomList<Automobile> cars, ToIntFunction<Automobile> field) {
        StringBuilder sb = new StringBuilder();
        for (Automobile car : cars) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(car.getModel()).append('(').append(field.applyAsInt(car)).append(')');
        }
        return sb.toString();
    }
    /// Элементы списка через пробел, например `1 2 3`.
    static String text(CustomList<?> list) {
        StringBuilder sb = new StringBuilder();
        for (Object element : list) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(element);
        }
        return sb.toString();
    }
}