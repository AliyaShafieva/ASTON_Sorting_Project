package aston.sort;

import aston.core.Automobile;
import aston.core.AutomobileComparators;
import aston.util.CustomList;

public class SortStrategyTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        SortStrategy[] strategies = {new InsertionSort(), new BubbleSort()};

        for (SortStrategy strategy : strategies) {
            System.out.println("=== " + strategy.name() + " ===");
            sortsByPower(strategy);
            sortsByModel(strategy);
            sortsByYear(strategy);
            keepsOrderOfEqualElements(strategy);
            sortsNumbersWithDuplicates(strategy);
            sortsAlreadySortedList(strategy);
            sortsReversedList(strategy);
            sortsMoreThanTenElements(strategy);
            emptyListDoesNotFail(strategy);
            singleElementStaysInPlace(strategy);
            System.out.println();
        }

        System.out.println("Итого: прошло " + passed + ", упало " + failed);
    }

    // ---------- автомобили ----------

    private static void sortsByPower(SortStrategy strategy) {
        CustomList<Automobile> cars = cars();
        strategy.sort(cars, AutomobileComparators.BY_POWER);
        check("по мощности", models(cars), "Lada Kia Audi BMW");
    }

    private static void sortsByModel(SortStrategy strategy) {
        CustomList<Automobile> cars = cars();
        strategy.sort(cars, AutomobileComparators.BY_MODEL);
        check("по модели", models(cars), "Audi BMW Kia Lada");
    }

    private static void sortsByYear(SortStrategy strategy) {
        CustomList<Automobile> cars = cars();
        strategy.sort(cars, AutomobileComparators.BY_YEAR);
        check("по году", models(cars), "Audi Lada Kia BMW");
    }

    /// Opel и Kia с одинаковой мощностью 120 должны остаться в исходном порядке.
    private static void keepsOrderOfEqualElements(SortStrategy strategy) {
        CustomList<Automobile> cars = new CustomList<>();
        cars.add(car("Opel", 120, 2010));
        cars.add(car("BMW", 250, 2020));
        cars.add(car("Kia", 120, 2018));
        cars.add(car("Lada", 90, 2015));
        strategy.sort(cars, AutomobileComparators.BY_POWER);
        check("равные остаются по порядку", models(cars), "Lada Opel Kia BMW");
    }

    // ---------- числа и граничные случаи ----------

    private static void sortsNumbersWithDuplicates(SortStrategy strategy) {
        CustomList<Integer> nums = numbers(5, 3, 5, 1, 9, 0);
        strategy.sort(nums, Integer::compare);
        check("числа с повторами", text(nums), "0 1 3 5 5 9");
    }

    private static void sortsAlreadySortedList(SortStrategy strategy) {
        CustomList<Integer> nums = numbers(1, 2, 3, 4, 5);
        strategy.sort(nums, Integer::compare);
        check("уже отсортированный", text(nums), "1 2 3 4 5");
    }

    private static void sortsReversedList(SortStrategy strategy) {
        CustomList<Integer> nums = numbers(5, 4, 3, 2, 1);
        strategy.sort(nums, Integer::compare);
        check("в обратном порядке", text(nums), "1 2 3 4 5");
    }

    /// Больше 10 элементов: CustomList расширяет внутренний массив.
    private static void sortsMoreThanTenElements(SortStrategy strategy) {
        CustomList<Integer> nums = numbers(15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
        strategy.sort(nums, Integer::compare);
        check("15 элементов", text(nums), "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
    }

    private static void emptyListDoesNotFail(SortStrategy strategy) {
        CustomList<Integer> nums = numbers();
        strategy.sort(nums, Integer::compare);
        check("пустой список", text(nums), "");
    }

    private static void singleElementStaysInPlace(SortStrategy strategy) {
        CustomList<Integer> nums = numbers(42);
        strategy.sort(nums, Integer::compare);
        check("один элемент", text(nums), "42");
    }

    // ---------- вспомогательное ----------

    private static void check(String name, String actual, String expected) {
        if (actual.equals(expected)) {
            passed++;
            System.out.println("OK    " + name + ": [" + actual + "]");
        } else {
            failed++;
            System.out.println("FAIL  " + name + ": ожидали [" + expected + "], получили [" + actual + "]");
        }
    }

    private static Automobile car(String model, int power, int year) {
        return new Automobile.Builder().model(model).power(power).year(year).build();
    }

    private static CustomList<Automobile> cars() {
        CustomList<Automobile> cars = new CustomList<>();
        cars.add(car("Lada", 90, 2015));
        cars.add(car("BMW", 250, 2020));
        cars.add(car("Audi", 190, 2012));
        cars.add(car("Kia", 120, 2018));
        return cars;
    }

    private static CustomList<Integer> numbers(int... values) {
        CustomList<Integer> list = new CustomList<>();
        for (int v : values) {
            list.add(v);
        }
        return list;
    }

    private static String models(CustomList<Automobile> cars) {
        StringBuilder sb = new StringBuilder();
        for (Automobile car : cars) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(car.getModel());
        }
        return sb.toString();
    }

    private static String text(CustomList<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (Integer n : list) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(n);
        }
        return sb.toString();
    }
}